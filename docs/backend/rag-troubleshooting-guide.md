# RAG 故障排查指南

> 本文档汇总 RAG 系统开发和运维过程中常见的故障场景、排查方法和解决方案

## 📋 目录

- [系统查询超时](#系统查询超时)
- [检索结果不准确](#检索结果不准确)
- [生成内容出现幻觉](#生成内容出现幻觉)
- [文档处理效果差](#文档处理效果差)
- [多轮对话效果差](#多轮对话效果差)
- [评估系统效果](#评估系统效果)
- [成本过高](#成本过高)

---

## 系统查询超时

### 可能原因

- 向量数据库未开启量化
- 未使用磁盘索引
- 未采用套娃策略降维

### 排查步骤

#### 1. 检查向量数据库配置
```bash
# 检查 Qdrant 配置
curl http://localhost:6333/collections/{collection_name}

# 检查是否开启量化
# 查看响应中的 quantization_config
```

#### 2. 检查索引状态
```python
# 检查向量数据库索引状态
collection_info = await client.get_collection(collection_name)
print("Vectors count:", collection_info.vectors_count)
print("Indexed vectors count:", collection_info.indexed_vectors_count)
```

#### 3. 性能监控
```python
# 添加查询时间监控
import time

start_time = time.time()
results = await vector_db.search(query_vector, limit=10)
elapsed = time.time() - start_time

if elapsed > 1.0:  # 超过1秒
    logger.warning(f"Slow query: {elapsed:.2f}s")
```

### 解决方案

#### 1. 开启标量量化
```yaml
# docker-compose.yml
services:
  qdrant:
    environment:
      - QDRANT__STORAGE__OPTIMIZATION_PERFORMANCE__MAX_OPTIMIZATION_THREADS=2
      - QDRANT__QUANTIZATION__SCALAR__TYPE=int8  # 开启 int8 量化
```

#### 2. 启用磁盘索引
```python
# 配置磁盘索引
await client.update_collection(
    collection_name=collection_name,
    optimizer_config=models.OptimizersConfigDiff(
        max_optimization_threads=2,
        indexing_threshold=10000,  # 内存中保持的向量数
        memmap_threshold=50000,   # 超过此数量向量存到磁盘
    )
)
```

#### 3. 使用套娃嵌入策略
```python
class MatryoshkaEmbedder:
    def __init__(self, base_embedder, target_dims: int = 256):
        self.embedder = base_embedder
        self.target_dims = target_dims

    async def embed(self, texts: List[str]) -> List[List[float]]:
        """生成套娃嵌入"""
        # 生成全维度向量
        full_vectors = await self.embedder.embed(texts)

        # 截断到目标维度
        return [vec[:self.target_dims] for vec in full_vectors]

# 使用示例
embedder = MatryoshkaEmbedder(openai_embedder, target_dims=256)
query_vector = await embedder.embed([query])
```

---

## 检索结果不准确

### 可能原因

- 单一向量搜索无法应对所有问题类型
- 未进行查询治理
- 未使用多路召回

### 排查步骤

#### 1. 分析查询类型分布
```python
class QueryAnalyzer:
    def analyze_query_types(self, queries: List[str]) -> Dict[str, int]:
        """分析查询类型分布"""
        types = {
            'factual': 0,      # 事实性问题
            'semantic': 0,     # 语义性问题
            'relational': 0,   # 关系型问题
            'procedural': 0    # 流程性问题
        }

        for query in queries:
            if self.is_factual_query(query):
                types['factual'] += 1
            elif self.is_relational_query(query):
                types['relational'] += 1
            elif self.is_procedural_query(query):
                types['procedural'] += 1
            else:
                types['semantic'] += 1

        return types
```

#### 2. 检查检索结果质量
```python
def evaluate_retrieval_quality(query: str, retrieved_docs: List[Dict]) -> Dict:
    """评估检索结果质量"""
    return {
        'query': query,
        'retrieved_count': len(retrieved_docs),
        'avg_relevance_score': sum(doc.get('score', 0) for doc in retrieved_docs) / len(retrieved_docs),
        'top_score': max((doc.get('score', 0) for doc in retrieved_docs), default=0),
        'source_distribution': self._analyze_sources(retrieved_docs)
    }
```

#### 3. A/B 测试不同检索策略
```python
class RetrievalTester:
    async def compare_strategies(self, query: str) -> Dict:
        """比较不同检索策略的效果"""

        # 策略A：仅向量检索
        vector_results = await self.vector_only_search(query)

        # 策略B：仅关键词检索
        keyword_results = await self.keyword_only_search(query)

        # 策略C：混合检索
        hybrid_results = await self.hybrid_search(query)

        return {
            'vector_only': self.evaluate_results(vector_results),
            'keyword_only': self.evaluate_results(keyword_results),
            'hybrid': self.evaluate_results(hybrid_results)
        }
```

### 解决方案

#### 1. 实施查询治理
```python
class QueryGovernor:
    def __init__(self):
        self.entity_patterns = {
            'order_id': r'\b\d{4,}\b',  # 订单号
            'product_id': r'\b[Pp]\d+\b',  # 产品ID
            'user_id': r'\b[Uu]\d+\b'  # 用户ID
        }

    def govern_query(self, query: str) -> Dict:
        """查询治理"""
        # 1. 实体识别
        entities = self.extract_entities(query)

        # 2. 语义清洗
        cleaned_query = self.clean_query(query)

        # 3. 查询分类
        query_type = self.classify_query(cleaned_query)

        # 4. 生成过滤条件
        filters = self.generate_filters(entities)

        return {
            'original_query': query,
            'cleaned_query': cleaned_query,
            'query_type': query_type,
            'entities': entities,
            'filters': filters
        }
```

#### 2. 智能路由到最适合的数据源
```python
class IntelligentRouter:
    def route_query(self, query_info: Dict) -> str:
        """智能路由"""

        query_type = query_info['query_type']
        entities = query_info['entities']

        # 规则引擎
        if query_type == 'factual' and entities.get('order_id'):
            return 'sql_database'  # 订单查询走 SQL

        elif query_type == 'relational':
            return 'graph_database'  # 关系查询走图数据库

        elif self._has_structured_patterns(query_info['cleaned_query']):
            return 'structured_search'  # 结构化查询

        else:
            return 'vector_search'  # 默认向量搜索
```

#### 3. 双路召回 + 重排序
```python
class HybridRetriever:
    async def retrieve(self, query: str, top_k: int = 20) -> List[Document]:
        """混合检索"""

        # 并行执行双路召回
        vector_task = self.vector_retrieve(query, top_k * 2)
        keyword_task = self.keyword_retrieve(query, top_k * 2)

        vector_results, keyword_results = await asyncio.gather(
            vector_task, keyword_task
        )

        # 融合排序 (RRF)
        combined = self.rrf_fusion(vector_results, keyword_results)

        # 重排序
        reranked = await self.rerank(query, combined[:top_k * 2])

        return reranked[:top_k]

    def rrf_fusion(self, list1: List, list2: List, k: int = 60) -> List:
        """RRF 融合算法"""
        scores = {}

        # 计算 RRF 分数
        for rank, doc in enumerate(list1, 1):
            scores[doc.id] = scores.get(doc.id, 0) + 1.0 / (k + rank)

        for rank, doc in enumerate(list2, 1):
            scores[doc.id] = scores.get(doc.id, 0) + 1.0 / (k + rank)

        # 排序
        sorted_docs = sorted(scores.items(), key=lambda x: x[1], reverse=True)

        return [doc_id for doc_id, score in sorted_docs]
```

---

## 生成内容出现幻觉

### 可能原因

- 未实施强引用约束
- 检索结果质量低
- 未进行内容标注

### 排查步骤

#### 1. 分析幻觉类型
```python
class HallucinationAnalyzer:
    def analyze_hallucinations(self, generated_text: str, context_docs: List[Dict]) -> Dict:
        """分析生成内容的幻觉情况"""

        issues = {
            'unsupported_claims': [],  # 无根据的声明
            'contradictory_info': [],  # 矛盾信息
            'fabricated_details': [], # 编造细节
            'temporal_errors': []     # 时间错误
        }

        # 检查是否基于上下文
        context_text = ' '.join(doc.get('content', '') for doc in context_docs)

        sentences = self.split_into_sentences(generated_text)
        for sentence in sentences:
            if not self.is_supported_by_context(sentence, context_text):
                issues['unsupported_claims'].append(sentence)

        return issues
```

#### 2. 检查 System Prompt 约束
```python
# 检查是否包含防幻觉约束
REQUIRED_CONSTRAINTS = [
    "只能基于提供的上下文信息",
    "不要根据你的训练知识",
    "如果找不到答案，请明确表示",
    "每个关键信息都要标注来源"
]

def validate_system_prompt(prompt: str) -> List[str]:
    """验证系统提示是否包含必要的防幻觉约束"""
    missing_constraints = []
    for constraint in REQUIRED_CONSTRAINTS:
        if constraint not in prompt:
            missing_constraints.append(constraint)
    return missing_constraints
```

### 解决方案

#### 1. 强约束 System Prompt
```python
ANTI_HALLUCINATION_PROMPT = """
你是一个基于检索增强的问答助手。请严格遵守以下规则：

1. **只能基于提供的上下文信息回答问题**
   - 不要使用你的训练数据或通用知识
   - 如果上下文没有相关信息，请明确说"无法找到答案"

2. **保持答案的客观性和准确性**
   - 不要推测或编造信息
   - 使用确切的上下文内容

3. **标注信息来源**
   - 每个重要信息后标注来源文档 ID
   - 格式：[来源: DOC-001]

4. **处理不确定情况**
   - 找不到答案时使用："根据现有资料，我无法找到确切的答案。"
   - 部分信息时说明："部分信息显示..."

上下文信息：
{context}

问题：{query}

请基于以上上下文回答："""

class AntiHallucinationGenerator:
    def __init__(self, llm_client):
        self.llm = llm_client

    async def generate(self, query: str, context_docs: List[Dict]) -> str:
        """防幻觉的生成"""

        # 格式化上下文
        context_text = self._format_context_with_sources(context_docs)

        # 构建约束提示
        prompt = ANTI_HALLUCINATION_PROMPT.format(
            context=context_text,
            query=query
        )

        # 生成回答
        response = await self.llm.generate(prompt, temperature=0.1)

        # 后处理验证
        if self._contains_hallucination_risks(response):
            return "根据现有资料，我无法提供确切的答案。"

        return response

    def _format_context_with_sources(self, docs: List[Dict]) -> str:
        """格式化带来源标注的上下文"""
        formatted = []
        for doc in docs:
            formatted.append(f"""
[文档 {doc['id']}]
标题: {doc.get('title', '未知')}
内容: {doc.get('content', '')}
[文档 {doc['id']} 结束]
""")
        return '\n'.join(formatted)

    def _contains_hallucination_risks(self, response: str) -> bool:
        """检查是否包含幻觉风险"""
        risk_patterns = [
            r'我认为', r'可能', r'大概', r'通常来说',
            r'一般情况下', r'据我所知', r'我相信'
        ]

        for pattern in risk_patterns:
            if re.search(pattern, response, re.IGNORECASE):
                return True

        return False
```

#### 2. 生成内容标注
```python
class ContentLabeler:
    def label_generated_content(self, response: str, context_docs: List[Dict]) -> str:
        """为生成内容添加来源标注"""

        # 解析响应中的引用
        references = self._extract_references(response)

        # 为每个引用添加标注
        labeled_response = response
        for ref in references:
            if ref['doc_index'] <= len(context_docs):
                doc = context_docs[ref['doc_index'] - 1]
                label = f"(来源: {doc['id']})"
                labeled_response = labeled_response.replace(
                    ref['text'], f"{ref['text']} {label}",
                    1  # 只替换第一次出现
                )

        return labeled_response

    def _extract_references(self, response: str) -> List[Dict]:
        """提取需要标注的引用"""
        references = []

        # 简单的引用检测逻辑
        sentences = re.split(r'[。！？]', response)

        for sentence in sentences:
            # 检查是否包含具体信息
            if self._contains_specific_info(sentence):
                references.append({
                    'text': sentence.strip(),
                    'doc_index': 1  # 简化处理
                })

        return references

    def _contains_specific_info(self, sentence: str) -> bool:
        """检查句子是否包含具体信息"""
        # 简单的启发式规则
        specific_indicators = [
            r'\d+',  # 数字
            r'第.+条',  # 条款
            r'.+规定',  # 规定
            r'.+要求',  # 要求
        ]

        return any(re.search(pattern, sentence) for pattern in specific_indicators)
```

#### 3. 提高 Precision@K
```python
class PrecisionOptimizer:
    def optimize_for_precision(self, retrieval_results: List[Dict], threshold: float = 0.7) -> List[Dict]:
        """通过提高查准率减少幻觉"""

        # 过滤低相关度结果
        filtered = [doc for doc in retrieval_results if doc.get('score', 0) >= threshold]

        # 如果过滤后结果太少，放宽阈值
        if len(filtered) < 3 and retrieval_results:
            threshold = min(threshold * 0.8, max(doc.get('score', 0) for doc in retrieval_results) * 0.9)
            filtered = [doc for doc in retrieval_results if doc.get('score', 0) >= threshold]

        return filtered

    def add_relevance_check(self, query: str, docs: List[Dict]) -> List[Dict]:
        """添加相关性二次检查"""

        enhanced_docs = []
        for doc in docs:
            # 使用轻量级模型进行相关性检查
            relevance_score = self._check_relevance(query, doc['content'])

            doc['relevance_score'] = relevance_score
            enhanced_docs.append(doc)

        # 按相关性重新排序
        enhanced_docs.sort(key=lambda x: x['relevance_score'], reverse=True)

        return enhanced_docs
```

---

## 文档处理效果差

### 可能原因

- 答案分散在不同页面
- Top-K 检索无法全部捕获
- 上下文被 LLM 截断

### 解决方案

#### 1. 使用层级索引
```python
class HierarchicalIndexer:
    def create_hierarchical_chunks(self, document: Dict) -> List[Dict]:
        """创建层级文档分块"""

        chunks = []

        # 1. 文档级块 (最大上下文)
        doc_chunk = {
            'id': f"{document['id']}_doc",
            'content': document['content'],
            'level': 'document',
            'parent_id': None,
            'metadata': {
                'title': document.get('title'),
                'type': 'full_document'
            }
        }
        chunks.append(doc_chunk)

        # 2. 章节级块
        chapters = self.extract_chapters(document['content'])
        for i, chapter in enumerate(chapters):
            chapter_chunk = {
                'id': f"{document['id']}_chapter_{i}",
                'content': chapter['content'],
                'level': 'chapter',
                'parent_id': doc_chunk['id'],
                'metadata': {
                    'chapter_title': chapter.get('title'),
                    'chapter_index': i
                }
            }
            chunks.append(chapter_chunk)

        # 3. 段落级块
        for chapter_chunk in chunks:
            if chapter_chunk['level'] == 'chapter':
                paragraphs = self.extract_paragraphs(chapter_chunk['content'])
                for j, para in enumerate(paragraphs):
                    para_chunk = {
                        'id': f"{chapter_chunk['id']}_para_{j}",
                        'content': para,
                        'level': 'paragraph',
                        'parent_id': chapter_chunk['id'],
                        'metadata': {
                            'paragraph_index': j
                        }
                    }
                    chunks.append(para_chunk)

        return chunks
```

#### 2. 实施多跳检索
```python
class MultiHopRetriever:
    async def multi_hop_retrieve(self, query: str, max_hops: int = 3) -> List[Dict]:
        """多跳检索"""

        all_results = []
        current_query = query

        for hop in range(max_hops):
            # 当前查询的检索
            results = await self.retrieve_single_hop(current_query)

            # 添加到结果集
            all_results.extend(results)

            # 生成下一跳查询
            next_query = await self.generate_next_hop_query(current_query, results)

            if not next_query or next_query == current_query:
                break  # 没有新的查询线索

            current_query = next_query

        # 去重和排序
        unique_results = self.deduplicate_results(all_results)

        return unique_results[:10]  # 返回 Top-10

    async def generate_next_hop_query(self, current_query: str, results: List[Dict]) -> Optional[str]:
        """生成下一跳查询"""

        # 从当前结果中提取线索
        clues = []
        for result in results[:3]:  # 只看前3个结果
            content = result.get('content', '')

            # 提取可能的线索词
            clue_words = self.extract_clue_words(content)
            clues.extend(clue_words)

        if not clues:
            return None

        # 构建新的查询
        new_query = f"{current_query} {' '.join(clues[:3])}"

        return new_query
```

#### 3. 配合章节摘要
```python
class DocumentSummarizer:
    async def create_chapter_summaries(self, document: Dict) -> Dict:
        """创建章节摘要"""

        chapters = self.extract_chapters(document['content'])
        summaries = {}

        for chapter in chapters:
            # 为每个章节生成摘要
            summary = await self.llm.summarize(
                text=chapter['content'],
                max_length=200,
                prompt="总结这个章节的主要内容和核心观点："
            )

            summaries[chapter.get('title', 'unknown')] = summary

        return summaries

    async def retrieve_with_summaries(self, query: str) -> List[Dict]:
        """基于摘要的检索"""

        # 首先检索摘要
        summary_results = await self.search_summaries(query)

        # 根据摘要结果找到对应章节
        chapter_results = []
        for summary_result in summary_results:
            chapter = await self.get_full_chapter(summary_result['chapter_id'])
            chapter_results.append(chapter)

        return chapter_results
```

---

## 多轮对话效果差

### 可能原因

- 未维护对话历史
- 历史信息干扰检索
- 缓存机制不当

### 解决方案

#### 1. 使用 Redis 维护对话历史队列
```python
class ConversationManager:
    def __init__(self, redis_client):
        self.redis = redis_client
        self.session_prefix = "conv:"

    async def add_message(self, session_id: str, role: str, content: str):
        """添加消息到对话历史"""

        message = {
            'role': role,
            'content': content,
            'timestamp': datetime.now().isoformat()
        }

        # 推入列表
        key = f"{self.session_prefix}{session_id}"
        await self.redis.lpush(key, json.dumps(message))

        # 保持最近 N 轮对话
        await self.redis.ltrim(key, 0, 19)  # 保留最近10轮（20条消息）

    async def get_recent_history(self, session_id: str, max_rounds: int = 5) -> List[Dict]:
        """获取最近的对话历史"""

        key = f"{self.session_prefix}{session_id}"
        messages_json = await self.redis.lrange(key, 0, max_rounds * 2 - 1)

        messages = []
        for msg_json in messages_json:
            messages.append(json.loads(msg_json))

        # 按时间排序（Redis lrange 返回的是从左到右，即新到旧）
        messages.reverse()

        return messages
```

#### 2. 实施 Query Rewriting
```python
class QueryRewriter:
    def __init__(self, llm_client):
        self.llm = llm_client

    async def rewrite_standalone_query(self, current_query: str, conversation_history: List[Dict]) -> str:
        """重写为独立完整的查询"""

        # 构建重写提示
        history_text = self.format_conversation_history(conversation_history)

        prompt = f"""
基于以下对话历史，将当前查询重写为一个独立、完整、语义明确的新查询。

对话历史:
{history_text}

当前用户查询: "{current_query}"

要求:
1. 新查询要包含所有必要的上下文信息
2. 避免代词和模糊引用，使用具体信息
3. 语义完整，可以独立理解
4. 适合用于文档检索

重写后的查询:"""

        # 调用 LLM 重写
        rewritten_query = await self.llm.generate(prompt, max_tokens=100)

        return rewritten_query.strip()

    def format_conversation_history(self, history: List[Dict]) -> str:
        """格式化对话历史"""
        formatted = []
        for msg in history[-5:]:  # 只用最近5轮
            role = "用户" if msg['role'] == 'user' else "助手"
            formatted.append(f"{role}: {msg['content']}")

        return "\n".join(formatted)
```

#### 3. 建立多维度缓存失效机制
```python
class SmartCacheManager:
    def __init__(self, redis_client):
        self.redis = redis_client

    def generate_cache_key(self, session_id: str, rewritten_query: str,
                          context_docs: List[Dict], knowledge_version: str) -> str:
        """生成智能缓存键"""

        # 1. 会话和查询
        session_hash = hashlib.md5(session_id.encode()).hexdigest()[:8]
        query_hash = hashlib.md5(rewritten_query.encode()).hexdigest()[:8]

        # 2. 上下文文档版本
        doc_ids = sorted([doc['id'] for doc in context_docs])
        docs_hash = hashlib.md5(''.join(doc_ids).encode()).hexdigest()[:8]

        # 3. 知识库版本
        kb_hash = hashlib.md5(knowledge_version.encode()).hexdigest()[:4]

        # 组合缓存键
        cache_key = f"rag:response:{session_hash}:{query_hash}:{docs_hash}:{kb_hash}"

        return cache_key

    async def get_cached_response(self, cache_key: str) -> Optional[str]:
        """获取缓存响应"""
        return await self.redis.get(cache_key)

    async def set_cached_response(self, cache_key: str, response: str, ttl: int = 1800):
        """设置缓存响应"""
        await self.redis.setex(cache_key, ttl, response)

    async def invalidate_session_cache(self, session_id: str):
        """会话结束时清理缓存"""
        pattern = f"rag:response:{hashlib.md5(session_id.encode()).hexdigest()[:8]}:*"
        await self._delete_by_pattern(pattern)

    async def invalidate_knowledge_cache(self, knowledge_version: str):
        """知识库更新时清理相关缓存"""
        pattern = f"rag:response:*:*:{hashlib.md5(knowledge_version.encode()).hexdigest()[:4]}"
        await self._delete_by_pattern(pattern)

    async def _delete_by_pattern(self, pattern: str):
        """按模式删除缓存"""
        cursor = 0
        while True:
            cursor, keys = await self.redis.scan(cursor, match=pattern, count=100)
            if keys:
                await self.redis.delete(*keys)
            if cursor == 0:
                break
```

#### 4. 仅保留最新几轮对话历史
```python
class OptimizedConversationManager:
    def __init__(self, redis_client, max_rounds: int = 3, max_tokens: int = 1000):
        self.redis = redis_client
        self.max_rounds = max_rounds
        self.max_tokens = max_tokens

    async def get_optimized_context(self, session_id: str) -> str:
        """获取优化后的对话上下文"""

        # 获取最近对话
        recent_messages = await self.get_recent_history(session_id, self.max_rounds)

        # 估算 token 数量
        context_text = ""
        for msg in reversed(recent_messages):  # 从旧到新
            msg_text = f"{msg['role']}: {msg['content']}\n"

            # 估算添加后总长度
            estimated_tokens = self.estimate_tokens(context_text + msg_text)

            if estimated_tokens > self.max_tokens:
                break

            context_text += msg_text

        return context_text.strip()

    def estimate_tokens(self, text: str) -> int:
        """估算文本的 token 数量"""
        # 简单估算：中文大约 1个汉字=1.5个token，英文1个单词=1.3个token
        chinese_chars = len(re.findall(r'[\u4e00-\u9fff]', text))
        english_words = len(text.split())

        return int(chinese_chars * 1.5 + english_words * 1.3)
```

---

## 评估系统效果

### 解决方案

#### 1. 建立黄金标准集
```python
class GoldenStandardBuilder:
    def __init__(self, db_client):
        self.db = db_client

    async def build_evaluation_dataset(self, sample_size: int = 100) -> List[Dict]:
        """建立评估数据集"""

        dataset = []

        # 从真实用户查询中采样
        real_queries = await self.db.get_real_user_queries(limit=sample_size)

        for query_record in real_queries:
            # 为每个查询创建标准答案
            standard_answer = await self.create_standard_answer(query_record)

            dataset.append({
                'input': query_record['query'],
                'expected_output': standard_answer,
                'tags': self.classify_query(query_record['query']),
                'context_required': self.identify_required_context(query_record)
            })

        return dataset

    async def create_standard_answer(self, query_record: Dict) -> str:
        """创建标准答案"""

        # 基于知识库人工标注或专家审核
        query = query_record['query']

        # 检索相关文档
        relevant_docs = await self.search_relevant_documents(query)

        # 专家标注标准答案
        standard_answer = await self.expert_annotation(query, relevant_docs)

        return standard_answer
```

#### 2. 使用核心指标
```python
class SystemEvaluator:
    def __init__(self, rag_system):
        self.rag_system = rag_system

    async def evaluate_system(self, test_dataset: List[Dict]) -> Dict[str, float]:
        """评估系统整体效果"""

        results = []

        for test_case in test_dataset:
            result = await self.evaluate_single_case(test_case)
            results.append(result)

        # 计算各项指标
        metrics = {
            'avg_precision@5': self.calculate_avg_precision(results, 5),
            'avg_recall@5': self.calculate_avg_recall(results, 5),
            'avg_f1@5': self.calculate_avg_f1(results, 5),
            'avg_hit_rate@5': self.calculate_avg_hit_rate(results, 5),
            'avg_response_time': self.calculate_avg_response_time(results)
        }

        return metrics

    async def evaluate_single_case(self, test_case: Dict) -> Dict:
        """评估单个测试案例"""

        query = test_case['input']
        expected_context = set(test_case.get('context_required', []))

        start_time = time.time()

        # 执行检索
        retrieved_docs = await self.rag_system.retrieve(query, top_k=10)

        # 生成答案
        generated_answer = await self.rag_system.generate(query, retrieved_docs)

        response_time = time.time() - start_time

        # 计算指标
        retrieved_context = set(doc['id'] for doc in retrieved_docs)

        return {
            'query': query,
            'retrieved_docs': retrieved_docs,
            'generated_answer': generated_answer,
            'response_time': response_time,
            'precision@5': len(retrieved_context & expected_context) / 5,
            'recall@5': len(retrieved_context & expected_context) / len(expected_context) if expected_context else 0,
            'hit_rate@5': 1.0 if len(retrieved_context & expected_context) > 0 else 0.0
        }
```

#### 3. 定期更新评估数据集
```python
class ContinuousEvaluator:
    def __init__(self, evaluator: SystemEvaluator, dataset_path: str):
        self.evaluator = evaluator
        self.dataset_path = dataset_path

    async def run_periodic_evaluation(self, interval_days: int = 7):
        """定期运行评估"""

        while True:
            try:
                # 加载最新数据集
                dataset = self.load_evaluation_dataset()

                # 运行评估
                metrics = await self.evaluator.evaluate_system(dataset)

                # 记录评估结果
                await self.record_evaluation_results(metrics)

                # 检查性能衰减
                await self.check_performance_degradation(metrics)

                # 等待下次评估
                await asyncio.sleep(interval_days * 24 * 3600)

            except Exception as e:
                logger.error(f"评估失败: {e}")
                await asyncio.sleep(3600)  # 1小时后重试

    async def check_performance_degradation(self, current_metrics: Dict):
        """检查性能衰减"""

        # 获取历史基准
        baseline = await self.get_baseline_metrics()

        # 检查关键指标
        for metric, current_value in current_metrics.items():
            baseline_value = baseline.get(metric)
            if baseline_value and current_value < baseline_value * 0.95:  # 下降5%
                await self.alert_performance_degradation(metric, current_value, baseline_value)
```

#### 4. 使用 LangSmith 或 MLflow 管理评估数据
```python
class LangSmithEvaluator:
    def __init__(self, langsmith_client):
        self.client = langsmith_client

    async def create_evaluation_dataset(self, name: str, dataset: List[Dict]):
        """创建 LangSmith 评估数据集"""

        dataset_obj = self.client.create_dataset(
            name=name,
            description="RAG 系统评估数据集"
        )

        for item in dataset:
            self.client.create_example(
                dataset_id=dataset_obj.id,
                input=item['input'],
                output=item['expected_output'],
                metadata={
                    'tags': item.get('tags', []),
                    'context_required': item.get('context_required', [])
                }
            )

    async def run_evaluation(self, dataset_name: str, evaluator_func):
        """运行评估"""

        # 创建评估任务
        evaluation = self.client.evaluate(
            evaluator_func,
            dataset_name=dataset_name,
            experiment_name=f"rag_eval_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        )

        return evaluation
```

---

## 成本过高

### 优化策略

#### 1. 向量存储降本
```yaml
# 使用量化版本的向量数据库
services:
  qdrant:
    image: qdrant/qdrant:latest
    environment:
      - QDRANT__QUANTIZATION__SCALAR__TYPE=int8  # 8位量化，节省75%存储
      - QDRANT__STORAGE__OPTIMIZATION_PERFORMANCE__MEMMAP_THRESHOLD=100000  # 磁盘索引

# 使用内存优化配置
  elasticsearch:
    environment:
      - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # 降低内存使用
      - indices.memory.index_buffer_size=10%  # 减少索引缓冲
```

##### 套娃策略（存储空间减少 6 倍）
```python
class CostOptimizedEmbedder:
    def __init__(self, embedder, target_dims: int = 256):
        self.embedder = embedder
        self.target_dims = target_dims  # 从1536维降到256维

    async def embed(self, texts: List[str]) -> List[List[float]]:
        """成本优化嵌入"""
        # 生成全维度向量
        full_vectors = await self.embedder.embed(texts)

        # 截断到目标维度，节省存储成本
        return [vec[:self.target_dims] for vec in full_vectors]

    def calculate_savings(self) -> Dict:
        """计算节省的成本"""
        original_dims = 1536
        target_dims = self.target_dims

        storage_savings = (original_dims - target_dims) / original_dims
        bandwidth_savings = storage_savings

        return {
            'storage_savings': f"{storage_savings:.1%}",
            'bandwidth_savings': f"{bandwidth_savings:.1%}",
            'estimated_monthly_savings': self.estimate_monthly_savings(storage_savings)
        }
```

#### 2. 语义缓存
```python
class SemanticCache:
    def __init__(self, redis_client, llm_client):
        self.redis = redis_client
        self.llm = llm_client
        self.hit_count = 0
        self.miss_count = 0

    async def get_or_generate(self, cache_key: str, generation_func, *args, **kwargs) -> str:
        """获取缓存或重新生成"""

        # 尝试获取缓存
        cached_result = await self.redis.get(cache_key)

        if cached_result:
            self.hit_count += 1
            return cached_result

        # 缓存未命中，生成结果
        self.miss_count += 1
        result = await generation_func(*args, **kwargs)

        # 写入缓存
        await self.redis.setex(cache_key, 3600, result)  # 1小时过期

        return result

    def get_hit_rate(self) -> float:
        """获取缓存命中率"""
        total = self.hit_count + self.miss_count
        return self.hit_count / total if total > 0 else 0

    async def generate_smart_cache_key(self, query: str, context_hash: str) -> str:
        """生成智能缓存键"""
        # 包含查询、上下文和系统版本等多维度信息
        key_components = [
            query,
            context_hash,
            self.llm.model_version,
            self.llm.system_prompt_hash
        ]

        combined = '|'.join(key_components)
        return f"rag:{hashlib.md5(combined.encode()).hexdigest()}"
```

#### 3. 查询优化
```python
class QueryOptimizer:
    def __init__(self):
        self.token_counter = TokenCounter()

    async def optimize_query_pipeline(self, query: str, conversation_history: List[Dict]) -> Dict:
        """优化查询处理流程"""

        # 1. 查询重写（减少上下文长度）
        standalone_query = await self.rewrite_query(query, conversation_history)

        # 2. 上下文压缩
        compressed_context = self.compress_conversation_history(conversation_history)

        # 3. Token 预算分配
        token_budget = self.allocate_token_budget(standalone_query, compressed_context)

        return {
            'optimized_query': standalone_query,
            'compressed_context': compressed_context,
            'token_budget': token_budget
        }

    async def rewrite_query(self, query: str, history: List[Dict]) -> str:
        """重写查询，减少依赖历史上下文"""

        if not history:
            return query

        # 使用轻量级提示重写
        rewrite_prompt = f"""
将用户查询改写为独立完整的查询：

历史上下文：{self.summarize_recent_history(history)}
当前查询：{query}

独立查询："""

        rewritten = await self.llm.generate(rewrite_prompt, max_tokens=50, temperature=0.1)
        return rewritten.strip()

    def compress_conversation_history(self, history: List[Dict], max_tokens: int = 500) -> str:
        """压缩对话历史"""

        compressed = []
        total_tokens = 0

        # 从最近的消息开始压缩
        for msg in reversed(history):
            msg_text = f"{msg['role']}: {msg['content']}"
            msg_tokens = self.token_counter.count(msg_text)

            if total_tokens + msg_tokens > max_tokens:
                # 总结剩余历史
                remaining = history[:len(history) - len(compressed)]
                summary = self.summarize_history(remaining)
                compressed.insert(0, f"历史总结: {summary}")
                break

            compressed.insert(0, msg_text)
            total_tokens += msg_tokens

        return '\n'.join(compressed)

    def allocate_token_budget(self, query: str, context: str) -> Dict:
        """分配 Token 预算"""

        query_tokens = self.token_counter.count(query)
        context_tokens = self.token_counter.count(context)

        # 预留生成空间
        generation_budget = 1000  # 预留给生成的 tokens

        total_budget = 4000  # 假设总预算4000 tokens
        system_budget = 200   # 系统提示

        available_for_context = total_budget - system_budget - generation_budget - query_tokens

        return {
            'total_budget': total_budget,
            'system_budget': system_budget,
            'query_budget': query_tokens,
            'context_budget': min(available_for_context, context_tokens),
            'generation_budget': generation_budget
        }
```

---

## 相关文档

- [RAG 检索架构设计](../architecture/rag-retrieval.md) - 系统架构总览
- [RAG 数据库设计](rag-database-design.md) - 数据存储设计
- [RAG 检索指南](rag-retrieval-guide.md) - 检索功能实现
- [RAG 生成指南](rag-generation-guide.md) - 生成内容实现
- [RAG 评估指南](rag-evaluation-guide.md) - 评估体系设计

---