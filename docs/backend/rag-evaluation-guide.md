# RAG 评估指南

> 本文档详细介绍 RAG 系统的评估方法，包括建立评估数据集、核心指标计算和持续优化策略

## 📋 目录

- [建立评估数据集](#建立评估数据集)
- [查看评估指标](#查看评估指标)
- [评估实施](#评估实施)
- [持续优化](#持续优化)

---

## 建立评估数据集

### 黄金标准集的特征

#### 1. 小而准原则
- 覆盖 3-5 类典型任务
- 每类 10 条左右样本
- 用以支撑第一版评估

#### 2. 代表业务规则
- 不只是"语言答案"
- 需要明确业务约束
- 体现业务流程

#### 3. 随业务演进
- 不是静态文档
- 企业政策变化时需更新
- 否则模型评估会偏离实际情况

#### 4. 结构化存储
- 格式：JSONL、CSV、YAML
- 要求：字段明确、可读性强、可被测试框架直接加载

### 构建示例：企业客服 Agent

#### 步骤 1：明确任务类型
- 订单查询
- 订单取消
- 发票相关
- 售后与投诉

#### 步骤 2：挑选典型样本
- 来源：真实客服工单、常见问题手册、业务流程图、产品政策文档
- 数量：每类 5-10 条

#### 步骤 3：配上标准答案
```json
{
  "input": "我的订单 8721 为什么还没发货？",
  "expected_output": "订单 8721 因供应链延迟，预计明天发货。",
  "tags": ["订单查询", "物流"],
  "context_required": ["订单状态查询流程", "物流延迟处理规则"]
}
```

#### 步骤 4：保存到评估工具
- LangSmith Datasets
- MLflow Model Registry
- Git 仓库（维护数据集版本）

---

## 查看评估指标

### 核心指标

#### 1. Precision@K（查准率）

**公式：**
```
Precision@K = (检索到的相关文档数) / K
```

**意义：**
- 衡量检索质量
- 回答："在系统返回的 K 个文档中，有多少是真正相关的"
- 高 Precision@K：检索结果中噪音少，降低幻觉可能性
- 低 Precision@K：可能是检索词太宽泛，结果不相关

#### 2. Recall@K（查全率）

**公式：**
```
Recall@K = (检索到的相关文档数) / (知识库中所有相关文档数)
```

**意义：**
- 衡量检索广度
- 回答："在知识库中所有真正相关的文档中，我们成功找到了多少"
- 高 Recall@K：未遗漏关键信息
- 低 Recall@K：即使大模型能力强，也会因缺乏正确事实依据而无法准确回答

#### 3. F1@K（调和平均数）

**公式：**
```
F1@K = 2 × (Precision@K × Recall@K) / (Precision@K + Recall@K)
```

**意义：**
- 衡量综合平衡性
- 只有当查准率和查全率都高时，F1@K 才会接近 1
- F1@K：调和平均值，反映系统整体效果

#### 4. HitRate@K（命中率）

**公式：**
```
HitRate@K = (至少命中一个正确文档的查询数) / (总查询数)
```

**意义：**
- 衡量基础成功率
- 回答："在前 K 个结果中，是否至少命中了一个正确的文档"
- 最基础的成功度量
- 常用于快速检验检索系统是否完全失效

### 指标应用

- **Recall@K**：反映嵌入模型的底层召回能力，决定答案是否在结果集中
- **Precision@K**：关注 Top-K 结果的纯净度，检验 Reranker 排序策略的有效性，是抵御幻觉的关键防线
- **F1@K**：综合平衡性
- **HitRate@K**：最基础的成功度量

---

## 评估实施

### 评估框架设计

```python
from typing import List, Dict, Any
from dataclasses import dataclass
import json

@dataclass
class EvaluationSample:
    """评估样本"""
    input_query: str
    expected_output: str
    tags: List[str]
    context_required: List[str]  # 需要的上下文文档

@dataclass
class EvaluationResult:
    """评估结果"""
    query: str
    retrieved_docs: List[Dict]
    generated_answer: str
    precision_at_k: Dict[int, float]
    recall_at_k: Dict[int, float]
    f1_at_k: Dict[int, float]
    hit_rate_at_k: Dict[int, float]

class RAGEvaluator:
    def __init__(self, rag_system):
        self.rag_system = rag_system

    async def evaluate_dataset(self, dataset: List[EvaluationSample]) -> Dict[str, Any]:
        """评估整个数据集"""
        results = []

        for sample in dataset:
            result = await self.evaluate_sample(sample)
            results.append(result)

        # 计算整体指标
        summary = self.calculate_summary_metrics(results)

        return {
            'results': results,
            'summary': summary
        }

    async def evaluate_sample(self, sample: EvaluationSample) -> EvaluationResult:
        """评估单个样本"""
        # 1. 执行检索
        retrieved_docs = await self.rag_system.retrieve(sample.input_query, top_k=10)

        # 2. 生成答案
        generated_answer = await self.rag_system.generate(
            sample.input_query,
            retrieved_docs
        )

        # 3. 计算指标
        metrics = self.calculate_metrics(retrieved_docs, sample.context_required)

        return EvaluationResult(
            query=sample.input_query,
            retrieved_docs=retrieved_docs,
            generated_answer=generated_answer,
            **metrics
        )

    def calculate_metrics(self, retrieved_docs: List[Dict],
                         required_contexts: List[str]) -> Dict[str, Any]:
        """计算各项指标"""

        k_values = [1, 3, 5, 10]
        metrics = {}

        # 获取检索到的文档ID
        retrieved_ids = {doc['id'] for doc in retrieved_docs}

        # 相关文档集合
        relevant_set = set(required_contexts)

        for k in k_values:
            # Top-K 检索结果
            top_k_ids = {doc['id'] for doc in retrieved_docs[:k]}

            # 计算指标
            precision = len(top_k_ids & relevant_set) / k if k > 0 else 0
            recall = len(top_k_ids & relevant_set) / len(relevant_set) if relevant_set else 0
            f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0
            hit_rate = 1.0 if len(top_k_ids & relevant_set) > 0 else 0.0

            metrics[f'precision_at_{k}'] = precision
            metrics[f'recall_at_{k}'] = recall
            metrics[f'f1_at_{k}'] = f1
            metrics[f'hit_rate_at_{k}'] = hit_rate

        return metrics

    def calculate_summary_metrics(self, results: List[EvaluationResult]) -> Dict[str, Any]:
        """计算汇总指标"""
        if not results:
            return {}

        k_values = [1, 3, 5, 10]
        summary = {}

        for k in k_values:
            precisions = [getattr(r, f'precision_at_{k}') for r in results]
            recalls = [getattr(r, f'recall_at_{k}') for r in results]
            f1s = [getattr(r, f'f1_at_{k}') for r in results]
            hit_rates = [getattr(r, f'hit_rate_at_{k}') for r in results]

            summary[f'avg_precision_at_{k}'] = sum(precisions) / len(precisions)
            summary[f'avg_recall_at_{k}'] = sum(recalls) / len(recalls)
            summary[f'avg_f1_at_{k}'] = sum(f1s) / len(f1s)
            summary[f'avg_hit_rate_at_{k}'] = sum(hit_rates) / len(hit_rates)

        return summary
```

### 评估数据集管理

```python
class EvaluationDataset:
    def __init__(self, file_path: str):
        self.file_path = file_path

    def load_dataset(self) -> List[EvaluationSample]:
        """加载评估数据集"""
        with open(self.file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        samples = []
        for item in data:
            samples.append(EvaluationSample(
                input_query=item['input'],
                expected_output=item['expected_output'],
                tags=item.get('tags', []),
                context_required=item.get('context_required', [])
            ))

        return samples

    def save_dataset(self, samples: List[EvaluationSample]):
        """保存评估数据集"""
        data = []
        for sample in samples:
            data.append({
                'input': sample.input_query,
                'expected_output': sample.expected_output,
                'tags': sample.tags,
                'context_required': sample.context_required
            })

        with open(self.file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    def add_sample(self, sample: EvaluationSample):
        """添加新样本"""
        samples = self.load_dataset()
        samples.append(sample)
        self.save_dataset(samples)

    def update_sample(self, index: int, sample: EvaluationSample):
        """更新样本"""
        samples = self.load_dataset()
        if 0 <= index < len(samples):
            samples[index] = sample
            self.save_dataset(samples)
```

### 评估报告生成

```python
class EvaluationReport:
    def __init__(self, evaluator: RAGEvaluator):
        self.evaluator = evaluator

    def generate_report(self, dataset_path: str, output_path: str = None) -> str:
        """生成评估报告"""

        # 加载数据集
        dataset = EvaluationDataset(dataset_path)
        samples = dataset.load_dataset()

        # 执行评估
        eval_results = asyncio.run(self.evaluator.evaluate_dataset(samples))

        # 生成报告
        report = self._format_report(eval_results)

        # 保存报告
        if output_path:
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write(report)

        return report

    def _format_report(self, eval_results: Dict[str, Any]) -> str:
        """格式化评估报告"""

        summary = eval_results['summary']
        results = eval_results['results']

        report = f"""
# RAG 系统评估报告

## 概览
- 总样本数: {len(results)}
- 评估时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## 汇总指标

| 指标 | @1 | @3 | @5 | @10 |
|------|----|----|----|----|
| Precision | {summary.get('avg_precision_at_1', 0):.3f} | {summary.get('avg_precision_at_3', 0):.3f} | {summary.get('avg_precision_at_5', 0):.3f} | {summary.get('avg_precision_at_10', 0):.3f} |
| Recall | {summary.get('avg_recall_at_1', 0):.3f} | {summary.get('avg_recall_at_3', 0):.3f} | {summary.get('avg_recall_at_5', 0):.3f} | {summary.get('avg_recall_at_10', 0):.3f} |
| F1 | {summary.get('avg_f1_at_1', 0):.3f} | {summary.get('avg_f1_at_3', 0):.3f} | {summary.get('avg_f1_at_5', 0):.3f} | {summary.get('avg_f1_at_10', 0):.3f} |
| Hit Rate | {summary.get('avg_hit_rate_at_1', 0):.3f} | {summary.get('avg_hit_rate_at_3', 0):.3f} | {summary.get('avg_hit_rate_at_5', 0):.3f} | {summary.get('avg_hit_rate_at_10', 0):.3f} |

## 详细结果

"""

        # 添加详细结果
        for i, result in enumerate(results[:10]):  # 只显示前10个
            report += f"""
### 样本 {i+1}
**查询:** {result.query}
**Precision@5:** {result.precision_at_k[5]:.3f}
**Recall@5:** {result.recall_at_k[5]:.3f}
**F1@5:** {result.f1_at_k[5]:.3f}

"""

        return report
```

---

## 持续优化

### 评估驱动优化

#### 1. 识别问题模式
```python
def analyze_weaknesses(eval_results: List[EvaluationResult]) -> Dict[str, Any]:
    """分析系统弱点"""

    weaknesses = {
        'low_recall_queries': [],
        'low_precision_queries': [],
        'failed_hit_rate_queries': []
    }

    for result in eval_results:
        if result.recall_at_k[5] < 0.5:
            weaknesses['low_recall_queries'].append(result.query)

        if result.precision_at_k[5] < 0.6:
            weaknesses['low_precision_queries'].append(result.query)

        if result.hit_rate_at_k[5] < 0.8:
            weaknesses['failed_hit_rate_queries'].append(result.query)

    return weaknesses
```

#### 2. 制定改进计划
```python
def create_improvement_plan(weaknesses: Dict[str, Any]) -> List[str]:
    """制定改进计划"""

    plan = []

    if weaknesses['low_recall_queries']:
        plan.append("🔍 改进检索广度：优化嵌入模型，提升召回率")
        plan.append("📚 扩展知识库：补充缺失的知识内容")
        plan.append("🔄 实现多跳检索：支持跨文档推理")

    if weaknesses['low_precision_queries']:
        plan.append("🎯 优化重排序：引入更强的 Cross-Encoder 模型")
        plan.append("🔧 改进查询处理：增强查询理解和扩展")
        plan.append("🏷️ 优化元数据：改善文档分类和标签")

    if weaknesses['failed_hit_rate_queries']:
        plan.append("🚀 改进索引策略：使用混合检索算法")
        plan.append("📊 数据质量：清洗和结构化知识库")
        plan.append("🔍 诊断检索问题：分析失败案例的根本原因")

    return plan
```

### 定期评估流程

#### 1. 建立评估基线
- 初始评估建立性能基线
- 设定关键指标的阈值
- 确定评估频率（每周/每月）

#### 2. 持续监控
```python
class ContinuousEvaluator:
    def __init__(self, evaluator: RAGEvaluator, dataset_path: str):
        self.evaluator = evaluator
        self.dataset_path = dataset_path
        self.baseline = None

    async def run_continuous_evaluation(self):
        """运行持续评估"""
        while True:
            # 执行评估
            results = await self.evaluator.evaluate_dataset_from_file(self.dataset_path)

            # 比较基线
            if self.baseline:
                changes = self.compare_with_baseline(results['summary'])

                # 触发告警或优化
                if self.should_alert(changes):
                    await self.send_alert(changes)

            # 更新基线
            self.baseline = results['summary']

            # 等待下次评估
            await asyncio.sleep(7 * 24 * 3600)  # 每周评估

    def compare_with_baseline(self, current: Dict) -> Dict:
        """比较当前结果与基线"""
        changes = {}
        for metric, value in current.items():
            if metric in self.baseline:
                baseline_value = self.baseline[metric]
                change = (value - baseline_value) / baseline_value * 100
                changes[metric] = change
        return changes

    def should_alert(self, changes: Dict) -> bool:
        """判断是否需要告警"""
        # 关键指标下降超过 5%
        key_metrics = ['avg_f1_at_5', 'avg_hit_rate_at_5']
        for metric in key_metrics:
            if metric in changes and changes[metric] < -5:
                return True
        return False
```

### A/B 测试框架

```python
class ABTestingFramework:
    def __init__(self, rag_system_a, rag_system_b):
        self.system_a = rag_system_a
        self.system_b = rag_system_b

    async def run_ab_test(self, dataset: List[EvaluationSample]) -> Dict[str, Any]:
        """运行 A/B 测试"""

        results_a = await self.evaluate_system(self.system_a, dataset)
        results_b = await self.evaluate_system(self.system_b, dataset)

        # 统计检验
        significance = self.statistical_test(results_a, results_b)

        return {
            'system_a': results_a,
            'system_b': results_b,
            'significance': significance,
            'winner': 'A' if results_a['avg_f1_at_5'] > results_b['avg_f1_at_5'] else 'B'
        }

    def statistical_test(self, results_a: Dict, results_b: Dict) -> float:
        """统计显著性检验"""
        # 使用 t-test 或 mann-whitney U test
        # 这里简化实现
        return 0.95  # p-value
```

---

## 常见问题

### Q1: 如何建立有效的评估数据集？

**解决方案：**
1. 从真实用户查询中抽样
2. 覆盖典型业务场景
3. 确保答案的唯一性和准确性
4. 定期更新数据集

### Q2: 评估指标偏低怎么办？

**解决方案：**
1. 分析失败案例，找出共性问题
2. 优化检索算法和参数
3. 改进数据质量
4. 增强模型能力

### Q3: 如何平衡不同指标？

**解决方案：**
1. 根据业务场景设置指标权重
2. 使用综合指标（如 F1 分数）
3. A/B 测试验证改进效果
4. 持续迭代优化

---

## 相关文档

- [RAG 检索架构设计](../architecture/rag-retrieval.md) - 系统架构总览
- [RAG 数据库设计](rag-database-design.md) - 数据存储设计
- [RAG 检索指南](rag-retrieval-guide.md) - 检索功能实现
- [RAG 生成指南](rag-generation-guide.md) - 生成内容实现

---