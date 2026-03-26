#!/bin/bash
# AIHub Git 提交脚本 - 推送到 Gitee 和 GitHub

set -e

cd "$(dirname "$0")/.."

# 检查参数
if [ -z "$1" ]; then
    echo "用法: ./scripts/git-commit.sh <提交消息>"
    echo "示例: ./scripts/git-commit.sh 'feat: 添加新功能'"
    exit 1
fi

COMMIT_MSG="$1"

# 清理 Python 缓存文件（不纳入追踪）
find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null || true
find . -type f -name "*.pyc" -delete 2>/dev/null || true
rm -f aihub-python/test_output.txt 2>/dev/null || true

# 检查是否有变更
if [ -z "$(git status --porcelain)" ]; then
    echo "没有变更需要提交"
    exit 0
fi

# 显示变更状态
echo "========== 变更文件 =========="
git status --short

# 添加所有变更
git add -A

# 提交
echo ""
echo "========== 提交中 =========="
git commit -m "$COMMIT_MSG"

# 推送到 Gitee (origin)
echo ""
echo "========== 推送到 Gitee =========="
git push origin master

# 推送到 GitHub (github)
echo ""
echo "========== 推送到 GitHub =========="
git push github master

echo ""
echo "✅ 完成！已推送到 Gitee 和 GitHub"
