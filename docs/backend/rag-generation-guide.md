# RAG 生成内容指南

> 本文档详细介绍 RAG 系统中生成内容的核心技术和实现方法，包括上下文打包、语义缓存、防幻觉等关键技术

## 📋 目录

- [上下文打包](#上下文打包)
- [语义缓存](#语义缓存)
- [遏制幻觉](#遏制幻觉)
- [实现指南](#实现指南)

---

## 上下文打包

### 核心策略

#### 1. 排序优化（三明治/沙漏策略）

**问题：** 大模型会忽略 Prompt 中间部分的关键信息

**解决：** 将最重要的 Top-K 文档放在 Prompt 的开头和结尾

**原理：** 利用大模型对边缘位置信息的敏感性

```python
def arrange_contexts(contexts: List[Document], strategy: str = "sandwich") -> str:
    """上下文排序策略"""
    if strategy == "sandwich":
        # 三明治策略：重要文档放在开头和结尾
        sorted_contexts = sorted(contexts, key=lambda x: x.score, reverse=True)
        arranged = []

        # 开头放最重要的
        arranged.extend(sorted_contexts[:2])

        # 中间放次重要的
        if len(sorted_contexts) > 4:
            arranged.extend(sorted_contexts[2:-2])

        # 结尾放次重要的
        if len(sorted_contexts) >= 4:
            arranged.extend(sorted_contexts[-2:])

        return arranged
```

#### 2. 边界清晰化

**方法：** 在每个文档块前后加上明确标记

**格式：**
```
[CONTEXT START ID - 001]
文档内容...
[CONTEXT END]
```

**作用：** 帮助大模型清晰识别上下文边界，避免视为连续文本

```python
def format_context_block(doc: Document, index: int) -> str:
    """格式化上下文块"""
    return f"""
[CONTEXT START ID - {doc.id}]
Source: {doc.title}
Content: {doc.content}
Relevance Score: {doc.score:.3f}
[CONTEXT END]
""".strip()
```

---

## 语义缓存

### 基础实践

#### 工具：Redis 或 Key-Value 存储

**方法：** 基于 Session ID 维护对话历史队列

**优化：** 仅把最新几轮的对话历史压缩或总结后送入

```python
class SemanticCache:
    def __init__(self, redis_client):
        self.redis = redis_client
        self.session_prefix = "rag:session:"

    async def get_cached_response(self, session_id: str, query: str) -> Optional[str]:
        """获取缓存的响应"""
        cache_key = self._generate_cache_key(session_id, query)
        return await self.redis.get(cache_key)

    async def set_cached_response(self, session_id: str, query: str, response: str, ttl: int = 3600):
        """设置缓存响应"""
        cache_key = self._generate_cache_key(session_id, query)
        await self.redis.setex(cache_key, ttl, response)

    def _generate_cache_key(self, session_id: str, query: str) -> str:
        """生成缓存键"""
        # 简单实现：基于 session_id 和 query 的哈希
        query_hash = hashlib.md5(query.encode()).hexdigest()[:8]
        return f"{self.session_prefix}{session_id}:{query_hash}"
```

#### 对话历史管理

```python
class ConversationManager:
    def __init__(self, max_rounds: int = 5):
        self.max_rounds = max_rounds
        self.history = []

    def add_message(self, role: str, content: str):
        """添加消息到历史"""
        self.history.append({"role": role, "content": content})

        # 保持最大轮数
        if len(self.history) > self.max_rounds * 2:  # 每轮包含问答
            self.history = self.history[-self.max_rounds * 2:]

    def get_recent_context(self, max_tokens: int = 2000) -> str:
        """获取最近的对话上下文"""
        context = ""
        for message in reversed(self.history):
            message_text = f"{message['role']}: {message['content']}\n"
            if len(context) + len(message_text) > max_tokens:
                break
            context = message_text + context

        return context.strip()
```

### 进阶实践：Query Rewriting

**方法：** 利用大模型将当前查询和历史对话结合，重写为独立、完整且语义明确的新查询

**优势：**
- 大幅减少 Prompt 长度
- 避免 LLM 在检索时受历史信息干扰

**示例：**
- 原查询："我的订单为什么还没发货？"
- 历史上下文："已查询过订单 8721"
- 重写后："订单 8721 发货了吗？物流状态如何？"

```python
class QueryRewriter:
    def __init__(self, llm_client):
        self.llm = llm_client

    async def rewrite_query(self, current_query: str, conversation_history: List[dict]) -> str:
        """重写查询，使其独立完整"""

        # 构建重写提示
        prompt = f"""
根据以下对话历史和当前查询，重写一个独立、完整、语义明确的新查询。

对话历史:
{self._format_history(conversation_history)}

当前查询: {current_query}

要求:
1. 新查询要包含所有必要的历史信息
2. 避免代词，使用具体信息
3. 语义完整，可以独立理解
4. 保持原始查询的核心意图

重写后的查询:"""

        # 调用 LLM 重写
        rewritten = await self.llm.generate(prompt, max_tokens=200)

        return rewritten.strip()

    def _format_history(self, history: List[dict]) -> str:
        """格式化对话历史"""
        formatted = []
        for msg in history[-3:]:  # 只用最近3轮
            formatted.append(f"{msg['role']}: {msg['content']}")
        return "\n".join(formatted)
```

---

## 生产级语义缓存

### 目的

**跳过 LLM 复杂推理过程，直接返回上一次生成结果**

### 缓存 Key 设计

```
Cache Key = HASH(Query + Document Version Hash + User Context + ...)
```

```python
class ProductionSemanticCache:
    def __init__(self, redis_client):
        self.redis = redis_client

    def generate_cache_key(self, query: str, context_docs: List[Document],
                          user_context: dict, doc_versions: dict) -> str:
        """生成生产级缓存键"""

        # 1. 查询内容
        query_hash = hashlib.sha256(query.encode()).hexdigest()

        # 2. 文档版本哈希（确保文档更新时缓存失效）
        doc_ids = sorted([doc.id for doc in context_docs])
        doc_versions_sorted = sorted(doc_versions.items())
        docs_hash = hashlib.sha256(
            f"{doc_ids}_{doc_versions_sorted}".encode()
        ).hexdigest()

        # 3. 用户上下文（权限、偏好等）
        user_hash = hashlib.sha256(
            json.dumps(user_context, sort_keys=True).encode()
        ).hexdigest()

        # 4. 模型版本（确保模型更新时缓存失效）
        model_version = "gpt-4-1106-preview"  # 示例

        # 组合生成最终缓存键
        cache_key = f"rag:response:{query_hash[:16]}:{docs_hash[:16]}:{user_hash[:8]}:{model_version}"

        return cache_key
```

### 多维度缓存失效机制

**单个的 `HASH(Query)` 缓存不可靠**

**缓存 Key 必须关联所有可能影响答案的因素**

- 当知识库核心文档更新时，更新 Document Version Hash
- 确保旧缓存结果自动失效

```python
class CacheInvalidator:
    def __init__(self, redis_client, db_client):
        self.redis = redis_client
        self.db = db_client

    async def invalidate_document_cache(self, document_id: int):
        """文档更新时使相关缓存失效"""

        # 获取所有包含此文档的缓存键模式
        pattern = f"rag:response:*:{self._get_doc_hash(document_id)}:*"

        # 删除匹配的缓存
        cursor = 0
        while True:
            cursor, keys = await self.redis.scan(cursor, match=pattern, count=100)
            if keys:
                await self.redis.delete(*keys)
            if cursor == 0:
                break

    async def invalidate_user_cache(self, user_id: int):
        """用户权限变更时使相关缓存失效"""
        # 类似实现...

    def _get_doc_hash(self, document_id: int) -> str:
        """获取文档版本哈希"""
        # 从数据库获取文档版本信息
        version_info = self.db.get_document_version(document_id)
        return hashlib.sha256(str(version_info).encode()).hexdigest()[:16]
```

---

## 遏制幻觉

### 1. 强引用约束

#### System Prompt 硬性约束

- 只能基于提供的检索结果进行回答
- 执行"不知即不答"原则
- 找不到答案时使用预设统一措辞：
  - "根据现有资料，我无法找到答案。"
  - "资料未提及。"

**作用：** 杜绝模型进行推测和编造

```python
SYSTEM_PROMPT = """
你是一个基于检索增强的问答助手。请严格遵守以下规则：

1. 只能基于提供的上下文信息回答问题
2. 如果上下文信息中没有相关内容，请明确表示"无法找到答案"
3. 不要根据你的训练知识进行推测或补充信息
4. 每个关键信息都要标注来源
5. 保持回答的客观性和准确性

上下文信息：
{context}

问题：{query}

请基于以上上下文回答："""

class HallucinationPreventer:
    def __init__(self, llm_client):
        self.llm = llm_client

    async def generate_answer(self, query: str, context_docs: List[Document]) -> str:
        """生成防幻觉的答案"""

        # 格式化上下文
        context_text = self._format_context(context_docs)

        # 构建系统提示
        prompt = SYSTEM_PROMPT.format(
            context=context_text,
            query=query
        )

        # 调用 LLM
        response = await self.llm.generate(prompt, temperature=0.1)

        # 后处理检查
        if self._contains_hallucination_indicators(response):
            return "根据现有资料，我无法找到确切的答案。"

        return response

    def _format_context(self, docs: List[Document]) -> str:
        """格式化上下文，确保来源可追溯"""
        formatted = []
        for i, doc in enumerate(docs, 1):
            formatted.append(f"""
[来源 {i}: {doc.title}]
{doc.content}
[来源 {i} 结束]
""")
        return "\n".join(formatted)

    def _contains_hallucination_indicators(self, response: str) -> bool:
        """检查是否包含幻觉指标"""
        hallucination_patterns = [
            r"我认为", r"可能", r"大概", r"通常来说",
            r"一般情况下", r"据我所知", r"我记得"
        ]

        for pattern in hallucination_patterns:
            if re.search(pattern, response):
                return True
        return False
```

### 2. 生成内容标注

#### 要求：每个关键信息后附带标注来源

**格式示例：**
- 用户问："退货期是多久？"
- AI 回答："标准退货期为 7 天。(来源: POLICY-2024-RETURN-003)"

**优势：**
- 提升用户信任度
- 答案可追溯性
- 强制 LLM 在生成时不断核对源文件
- 有效抑制模型偏离事实的倾向

```python
class ContentLabeler:
    def __init__(self):
        self.source_pattern = re.compile(r'\[来源 (\d+): ([^\]]+)\]')

    def label_content(self, response: str, context_docs: List[Document]) -> str:
        """为生成内容添加来源标注"""

        # 解析响应中的引用
        references = self._extract_references(response)

        # 为每个引用添加标注
        labeled_response = response
        for ref in references:
            if ref['doc_index'] <= len(context_docs):
                doc = context_docs[ref['doc_index'] - 1]
                label = f"(来源: {doc.id})"
                labeled_response = labeled_response.replace(
                    ref['text'], f"{ref['text']} {label}"
                )

        return labeled_response

    def _extract_references(self, response: str) -> List[dict]:
        """提取响应中的引用"""
        references = []

        # 查找可能的引用点
        sentences = re.split(r'[。！？]', response)

        for sentence in sentences:
            # 简单的引用检测逻辑
            if any(keyword in sentence for keyword in ['退货', '发货', '支付', '订单']):
                references.append({
                    'text': sentence.strip(),
                    'doc_index': 1  # 简化处理，实际需要更复杂的映射
                })

        return references
```

---

## 实现指南

### 完整的生成流程

```python
class RAGGenerator:
    def __init__(self, cache: SemanticCache, rewriter: QueryRewriter,
                 hallucination_preventer: HallucinationPreventer):
        self.cache = cache
        self.rewriter = rewriter
        self.preventer = hallucination_preventer

    async def generate(self, query: str, context_docs: List[Document],
                      conversation_history: List[dict] = None) -> str:
        """完整的 RAG 生成流程"""

        # 1. 查询重写（如果有对话历史）
        if conversation_history:
            query = await self.rewriter.rewrite_query(query, conversation_history)

        # 2. 尝试缓存命中
        cached_response = await self.cache.get_cached_response(query, context_docs)
        if cached_response:
            return cached_response

        # 3. 生成回答
        response = await self.preventer.generate_answer(query, context_docs)

        # 4. 添加内容标注
        labeled_response = self._add_source_labels(response, context_docs)

        # 5. 缓存结果
        await self.cache.set_cached_response(query, context_docs, labeled_response)

        return labeled_response

    def _add_source_labels(self, response: str, context_docs: List[Document]) -> str:
        """添加来源标注"""
        labeler = ContentLabeler()
        return labeler.label_content(response, context_docs)
```

### 性能优化

1. **缓存策略**
   - 多层缓存：内存缓存 + Redis 缓存
   - 智能过期：基于内容新鲜度
   - 压缩存储：减少内存占用

2. **并发处理**
   - 异步生成：避免阻塞
   - 批量处理：提高 GPU 利用率
   - 队列管理：控制并发数量

3. **内容优化**
   - 上下文压缩：保留关键信息
   - 长度控制：避免超出 Token 限制
   - 格式标准化：统一输出格式

---

## 常见问题

### Q1: 如何处理上下文过长？

**解决方案：**
1. 上下文压缩（Context Compression）
2. 选择性保留（Selective Retention）
3. 分层生成（Hierarchical Generation）

### Q2: 缓存命中率太低怎么办？

**解决方案：**
1. 优化缓存键设计
2. 实现相似查询匹配
3. 使用语义缓存而非精确匹配

### Q3: 如何检测和防止幻觉？

**解决方案：**
1. 强约束系统提示
2. 事实核查机制
3. 置信度评分
4. 人工审核流程

---

## 相关文档

- [RAG 检索架构设计](../architecture/rag-retrieval.md) - 系统架构总览
- [RAG 数据库设计](rag-database-design.md) - 数据存储设计
- [RAG 检索指南](rag-retrieval-guide.md) - 检索功能实现
- [RAG 评估指南](rag-evaluation-guide.md) - 评估体系设计

---