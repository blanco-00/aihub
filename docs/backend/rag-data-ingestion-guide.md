# RAG 数据入库指南

> 本文档详细介绍 RAG 系统中数据入库的核心技术和实现方法，包括文档整理、结构化处理和存储策略

## 📋 目录

- [文档整理误区](#文档整理误区)
- [文档结构化](#文档结构化)
- [数据存储](#数据存储)

---

## 文档整理误区

### 误区 1：知识库 = 杂物间

**错误认知：** 把所有文档、聊天记录、会议纪要一股脑扔进去

**现实问题：**
- 检索出过时的旧版政策
- 包含未通过的草案内容
- 语义污染，稀释正确答案权重

**解决方案：** 构建经过清洗的"黄金数据集"

### 误区 2：电子书 = 速读大师

**错误认知：** 认为 RAG 能像人类一样理解全书的起承转合

**现实问题：**
- 只能找到孤立片段
- 无法总结人物成长弧光
- 缺乏全局视野（Global Context）

**解决方案：**
- 章节摘要（Summarization）
- 知识图谱（Knowledge Graph）构建宏观索引
- 多跳检索（Multi-hop Retrieval）

### 误区 3：文件格式 = 结构化

**错误认知：** PDF、Word 里的文字人眼看着整齐，AI 读出来也整齐

**现实问题：**
- 双栏排版被横着读成乱码
- 表格数字与表头错位
- 时间信息关联错误

**解决方案：**
- 版面分析（Layout Analysis）
- 表格转化为 Markdown/HTML
- 多栏还原为单栏

### 误区 4：长文档 = 超强记忆力

**错误认知：** 上传 300 页白皮书就能融会贯通

**现实问题：**
- 只能取回 Top-K（3-5）个切片
- 答案分散在不同页面时无法全部捕获
- 上下文被 LLM 截断

**解决方案：**
- 层级索引（Parent-Child Indexing）
- 多跳检索（Multi-hop Retrieval）

---

## 文档结构化

### 通用元数据外壳

每个知识块必须封装统一的元数据：

1. **唯一身份（ID）**
   - 格式：`POLICY-2024-RETURN-003`
   - 作用：精确溯源，便于更新与删除

2. **内容摘要**
   - 要求：15 字以内的核心概括
   - 示例："7 天无理由退货条件"
   - 作用：作为向量检索的高权重"语义锚点"

3. **生效时空**
   - 生命周期：如"2025年有效"
   - 适用范围：如"仅限华东直营店"
   - 作用：检索前过滤过期或异地规则

4. **关联网络**
   - 记录"上位文件"与"下位细则"的链接
   - 构建知识图谱，防止断章取义

5. **版本跟踪**
   - 更新日期
   - 更新人
   - 便于定期维护

### 场景化内容内核

针对不同知识类型采用定制化结构：

1. **问答类（FAQ）数据**
   - 结构：问题-答案-衍生追问（三元组）
   - 优势：提升多轮对话效果

2. **制度类（Policy）数据**
   - 结构：章节-条款-细则（嵌套结构）
   - 优势：避免混淆"通用条款"与"特例条款"

3. **流程类（Workflow）数据**
   - 结构：触发条件 → 节点动作 → 输出结果（线性逻辑链）
   - 优势：理解因果关系，回答时序性问题

4. **合同类（Contract）数据**
   - 结构：条款类型-约束对象-法律效力（关系矩阵）
   - 优势：清晰剥离甲方与乙方的权利义务

---

## 数据存储

### 1. 文件处理分级：三级回退策略

**极速层（Optimistic Parsing）**
- 工具：pypdf 等轻量级库
- 场景：数字原生文档
- 特点：速度最快、成本为零
- 覆盖率：80% 的规范文件

**结构层（Layout-Aware Parsing）**
- 工具：pdfplumber、LayoutParser
- 场景：乱码或提取置信度不足时
- 特点：恢复版面结构，确保表格、多栏排版准确

**视觉层（Optical Fallback）**
- 工具：AWS Textract 等 OCR
- 场景：扫描件或完全无法解析的文档
- 特点：成本高昂，但确保零遗漏

### 2. 向量嵌入：混合策略与存储降本

**套娃策略（Matryoshka Embeddings）**
- 适用场景：50W+ 文档
- 技术原理：弹性维度特性
- 效果：1536 维 → 256 维，存储空间减少 6 倍
- 模型：OpenAI text-embedding-3 系列、BGE-M3

**量化与磁盘卸载**
- **标量量化（Scalar Quantization）**：32 位浮点数 → 8 位整数
  - 效果：内存占用减少 4 倍
- **磁盘索引（On-disk Storage）**：冷数据从内存卸载至磁盘
  - 支撑能力：十万级乃至百万级向量索引

### 3. 多源异构融合

**关系型数据库（SQL）**
- 场景：库存、财务等精确数值类问题
- 技术：Text-to-SQL
- 优势：获取精准的实时数据

**图数据库（Knowledge Graph）**
- 场景：实体之间复杂关系的推理
- 工具：Neo4j、NebulaGraph
- 优势：多跳（Multi-hop）关系推理，扁平向量数据无法做到

**向量数据库（Vector Storage）**
- 场景：非结构化文本的语义检索
- 工具：Qdrant、Milvus、Weaviate

**异构融合架构**
- 系统演变为"智能路由器"
- 根据用户问题意图自动判断数据源
- 多路召回信息融合生成

---

## 实现指南

### 文档清洗流程

```python
class DocumentCleaner:
    def __init__(self, rules: List[CleaningRule]):
        self.rules = rules

    async def clean_document(self, document: Dict) -> Dict:
        """清洗文档，构建黄金数据集"""

        cleaned = document.copy()

        for rule in self.rules:
            if rule.applies_to(cleaned):
                cleaned = await rule.apply(cleaned)

        return cleaned

class CleaningRule:
    def applies_to(self, document: Dict) -> bool:
        """判断规则是否适用"""
        raise NotImplementedError

    async def apply(self, document: Dict) -> Dict:
        """应用清洗规则"""
        raise NotImplementedError

class DraftDocumentRule(CleaningRule):
    """过滤草稿文档"""

    def applies_to(self, document: Dict) -> bool:
        content = document.get('content', '').lower()
        return 'draft' in content or '草稿' in content

    async def apply(self, document: Dict) -> Dict:
        # 标记为需要人工审核
        document['needs_review'] = True
        document['review_reason'] = '包含草稿内容'
        return document
```

### 文档结构化处理

```python
class DocumentStructurer:
    def __init__(self, metadata_extractors: List[MetadataExtractor]):
        self.extractors = metadata_extractors

    async def structure_document(self, document: Dict) -> Dict:
        """结构化文档"""

        # 1. 提取通用元数据
        metadata = await self.extract_universal_metadata(document)

        # 2. 确定文档类型
        doc_type = self.classify_document_type(document)

        # 3. 应用场景化结构
        structured_content = await self.apply_scenario_structure(document, doc_type)

        return {
            'original_document': document,
            'metadata': metadata,
            'doc_type': doc_type,
            'structured_content': structured_content
        }

    async def extract_universal_metadata(self, document: Dict) -> Dict:
        """提取通用元数据"""

        metadata = {}

        for extractor in self.extractors:
            field_metadata = await extractor.extract(document)
            metadata.update(field_metadata)

        return metadata

class UniqueIdExtractor(MetadataExtractor):
    """唯一身份提取器"""

    async def extract(self, document: Dict) -> Dict:
        # 基于文档标题和时间生成唯一ID
        title = document.get('title', '')
        created_at = document.get('created_at', datetime.now())

        # 生成格式: DOC-2024-001
        base_id = f"DOC-{created_at.year}-{created_at.month:02d}"
        unique_id = f"{base_id}-{hash(title) % 1000:03d}"

        return {'unique_id': unique_id}

class SummaryExtractor(MetadataExtractor):
    """内容摘要提取器"""

    def __init__(self, llm_client):
        self.llm = llm_client

    async def extract(self, document: Dict) -> Dict:
        content = document.get('content', '')

        prompt = f"用15字以内总结以下内容的核心要点：\n\n{content[:1000]}..."

        summary = await self.llm.generate(prompt, max_tokens=20)

        return {'summary': summary.strip()}
```

### 多级解析策略

```python
class MultiLevelParser:
    def __init__(self, parsers: Dict[str, DocumentParser]):
        self.parsers = parsers  # {'optimistic': ..., 'layout': ..., 'ocr': ...}

    async def parse_document(self, file_path: str) -> Dict:
        """多级文档解析"""

        # 1. 尝试极速解析
        try:
            result = await self.parsers['optimistic'].parse(file_path)
            if self._is_confident(result):
                return result
        except Exception as e:
            logger.warning(f"Optimistic parsing failed: {e}")

        # 2. 回退到结构层解析
        try:
            result = await self.parsers['layout'].parse(file_path)
            if self._is_confident(result):
                return result
        except Exception as e:
            logger.warning(f"Layout parsing failed: {e}")

        # 3. 最终回退到OCR
        try:
            result = await self.parsers['ocr'].parse(file_path)
            return result
        except Exception as e:
            logger.error(f"All parsing methods failed: {e}")
            raise DocumentParsingError("Unable to parse document")

    def _is_confident(self, result: Dict) -> bool:
        """判断解析结果是否可信"""

        confidence = result.get('confidence', 0)

        # 检查文本长度是否合理
        text_length = len(result.get('content', ''))

        # 检查是否有表格被正确识别
        tables_found = len(result.get('tables', []))

        return confidence > 0.8 and text_length > 100
```

---

## 相关文档

- [RAG 数据库设计](rag-database-design.md) - 数据存储设计
- [RAG 检索指南](rag-retrieval-guide.md) - 检索功能实现
- [RAG 生成指南](rag-generation-guide.md) - 生成内容实现
- [RAG 评估指南](rag-evaluation-guide.md) - 评估体系设计
- [RAG 故障排查指南](rag-troubleshooting-guide.md) - 常见问题解决方案

---