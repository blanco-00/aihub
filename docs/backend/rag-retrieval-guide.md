# RAG 索引检索指南

> 本文档详细介绍 RAG 系统中索引检索的核心技术和实现方法，包括查询治理、智能路由、多路召回等关键环节

## 📋 目录

- [三层检索架构](#三层检索架构)
- [查询治理](#查询治理)
- [智能路由](#智能路由)
- [多路召回](#多路召回)
- [实现指南](#实现指南)

---

## 三层检索架构

```
查询治理 → 智能路由 → 多路召回
```

### 第一关：查询治理

**目标：** 清洗和规范化用户输入

#### 1. 语义清洗
- 修正错别字
- 还原缩写（"Q3" → "第三季度"）
- 解析相对时间（"上周" → 2024-10-14 ~ 2024-10-20）

#### 2. 元数据提取
- 识别关键实体（人名、地名、ID）
- 转换为数据库字段（"小明" → User_ID: 8821）
- 作为硬过滤条件（Pre-filter）

### 第二关：智能路由

**目标：** 根据问题类型路由到最适合的数据源

#### 路径 A：结构化数据查询（Text-to-SQL）
- 场景："上个月华东区的销售总额是多少？"
- 路由：MySQL/数据仓库
- 优势：精确查询，向量搜索无法算出准确总额

#### 路径 B：知识图谱推理（Text-to-Cypher）
- 场景："这家供应商的母公司的实际控制人是谁？"
- 路由：图数据库（Neo4j）
- 优势：多跳关系推理，扁平向量数据无法做到

#### 路径 C：非结构化语义检索（Vector Store）
- 场景："如果不满意想退款，具体流程是什么？"
- 路由：向量数据库（Qdrant/Milvus）
- 优势：处理模糊的、基于自然语言的知识查询

### 第三关：多路召回

#### 第一阶段：双路并行召回

**目标：** "宁可错杀，不可漏网"，快速捞取 Top-100 候选片段

##### 下路：文本检索（BM25/TF-IDF）
- 原理：基于倒排索引（Inverted Index）
- 特点：不关心语义，只关心词频
- 优势：
  - 专治"硬指标"（如错误码、产品型号）
  - 工具：
    - 轻量级：Rank-BM25（Python 库）
    - 生产级：Elasticsearch / OpenSearch
    - 现代推荐：Qdrant / Milvus（支持 Sparse Vectors）

##### 上路：向量检索（Bi-Encoder）
- 原理：将 Query 和 Document 分别映射为向量，计算余弦相似度
- 特点：捕捉"言外之意"
- 优势：语义匹配（"怎么退钱" → "退款流程"）
- 模型：
  - BGE-M3（最强开源多语言模型）
  - OpenAI text-embedding-3-small
  - 数据库：Qdrant（开启量化与磁盘索引）、Weaviate

##### 套娃策略的查询执行流程：
1. 生成全维度向量（1536 维）
2. 实时维度截断（1536 维 → 256 维）
3. 降维相似度计算（在 256 维空间执行）
4. 多路召回结果融合生成

#### 第二阶段：融合与重排序

##### 1. 加权排序（Weighted Ranking / RRF）
- 算法：RRF（倒数排名融合）
- 原理：谁排名靠前，谁的权重就高
- 结果：生成去重的候选列表
- 工具：
  - LangChain 的 EnsembleRetriever
  - LlamaIndex 的 QueryFusionRetriever

##### 2. 重排序（Cross-Encoder Reranking）
- **Bi-Encoder（召回阶段）**：分别看问题和答案，速度快但了解浅
- **Cross-Encoder（重排阶段）**：将问题和文档拼在一起逐字分析相关性
- 流程：对 Top-100 候选文档重新打分，保留 Top-5
- 模型：
  - 开源：BGE-Reranker-v2-m3、Jina-Reranker
  - 商业：Cohere Reranker（业界标杆，支持微调）
- 优势：显著提升 Top-K 结果相关性

---

## 实现指南

### 查询治理实现

```python
class QueryGovernor:
    def clean_query(self, query: str) -> dict:
        """查询治理：语义清洗和元数据提取"""
        # 1. 语义清洗
        cleaned = self.correct_typos(query)
        cleaned = self.expand_abbreviations(cleaned)
        cleaned = self.parse_temporal_expressions(cleaned)

        # 2. 元数据提取
        entities = self.extract_entities(cleaned)
        filters = self.convert_to_filters(entities)

        return {
            'cleaned_query': cleaned,
            'filters': filters,
            'entities': entities
        }
```

### 智能路由实现

```python
class IntelligentRouter:
    def route_query(self, query: str, context: dict) -> str:
        """根据查询类型智能路由"""
        if self.is_structured_query(query):
            return "text_to_sql"
        elif self.is_relationship_query(query):
            return "knowledge_graph"
        else:
            return "vector_search"

    def is_structured_query(self, query: str) -> bool:
        """判断是否为结构化查询"""
        patterns = [
            r'多少', r'统计', r'总计', r'平均', r'最大', r'最小',
            r'销量', r'销售额', r'数量', r'排名'
        ]
        return any(re.search(pattern, query) for pattern in patterns)
```

### 多路召回实现

```python
class MultiSourceRetriever:
    async def retrieve(self, query: str, top_k: int = 100) -> List[Document]:
        """多路并行召回"""

        # 并行执行两种检索
        semantic_task = self.semantic_retrieve(query, top_k)
        keyword_task = self.keyword_retrieve(query, top_k)

        semantic_results, keyword_results = await asyncio.gather(
            semantic_task, keyword_task
        )

        # 融合排序
        combined = self.fuse_results(semantic_results, keyword_results)

        # 重排序
        reranked = await self.rerank(query, combined, top_k=10)

        return reranked

    async def semantic_retrieve(self, query: str, top_k: int) -> List[Document]:
        """向量语义检索"""
        # 1. 生成查询向量
        query_vector = await self.embedding_service.embed(query)

        # 2. 向量相似度搜索
        results = await self.vector_db.search(
            query_vector=query_vector,
            limit=top_k,
            filters=self.get_filters()
        )

        return results

    async def keyword_retrieve(self, query: str, top_k: int) -> List[Document]:
        """关键词检索"""
        # BM25 搜索
        results = await self.search_engine.search(
            query=query,
            size=top_k,
            index=self.index_name
        )

        return results
```

### 融合与重排序实现

```python
class ResultAggregator:
    def fuse_results(self, semantic_results: List, keyword_results: List) -> List:
        """RRF 融合算法"""
        fused_scores = {}

        # RRF 算法实现
        for rank, doc in enumerate(semantic_results, 1):
            doc_id = doc.id
            fused_scores[doc_id] = fused_scores.get(doc_id, 0) + 1.0 / (60 + rank)

        for rank, doc in enumerate(keyword_results, 1):
            doc_id = doc.id
            fused_scores[doc_id] = fused_scores.get(doc_id, 0) + 1.0 / (60 + rank)

        # 按融合分数排序
        sorted_docs = sorted(fused_scores.items(), key=lambda x: x[1], reverse=True)

        return [doc for doc_id, score in sorted_docs[:100]]

    async def rerank(self, query: str, candidates: List[Document], top_k: int) -> List[Document]:
        """Cross-Encoder 重排序"""
        if not candidates:
            return []

        # 准备输入
        query_doc_pairs = [(query, doc.content) for doc in candidates]

        # Cross-Encoder 打分
        scores = await self.reranker.predict(query_doc_pairs)

        # 按分数排序
        scored_docs = list(zip(candidates, scores))
        scored_docs.sort(key=lambda x: x[1], reverse=True)

        return [doc for doc, score in scored_docs[:top_k]]
```

---

## 性能优化

### 检索速度优化

1. **索引优化**
   - 选择合适的索引类型 (HNSW / IVF)
   - 预热索引，避免冷启动

2. **缓存策略**
   - 查询结果缓存 (Redis)
   - 向量缓存，避免重复计算

3. **并行处理**
   - 多路召回并行执行
   - GPU 加速向量计算

### 准确率优化

1. **查询增强**
   - Query Expansion（查询扩展）
   - Pseudo Relevance Feedback（伪相关反馈）

2. **结果重排序**
   - 多样性优化 (MMR)
   - 用户行为反馈

3. **持续学习**
   - 基于用户点击的排序学习
   - A/B 测试不同检索策略

---

## 常见问题

### Q1: 检索速度太慢怎么办？

**解决方案：**
1. 开启向量量化（int8）
2. 使用套娃策略降维
3. 启用磁盘索引
4. 实施两阶段检索

### Q2: 检索结果不准确怎么办？

**解决方案：**
1. 实施查询治理
2. 使用多路召回
3. 引入重排序模型
4. 优化索引策略

### Q3: 如何平衡速度和准确率？

**权衡策略：**
- 开发阶段：优先准确率
- 生产阶段：速度优先，准确率保障
- 使用两阶段检索：粗排保证速度，重排保证准确率

---

## 相关文档

- [RAG 检索架构设计](../architecture/rag-retrieval.md) - 系统架构总览
- [RAG 数据库设计](rag-database-design.md) - 数据存储设计
- [RAG 生成指南](rag-generation-guide.md) - 生成内容实现
- [RAG 评估指南](rag-evaluation-guide.md) - 评估体系设计

---