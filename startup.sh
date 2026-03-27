#!/bin/bash
#
# AIHub 统一启动脚本
#
# 用法:
#   ./startup.sh          - 启动所有服务
#   ./startup.sh java     - 只启动 Java 后端
#   ./startup.sh python   - 只启动 Python AI
#   ./startup.sh frontend - 只启动前端
#   ./startup.sh stop     - 停止所有服务
#   ./startup.sh status   - 查看服务状态
#

set -e

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# 服务端口
FRONTEND_PORT=9527
JAVA_PORT=9528
PYTHON_PORT=9529

# 日志目录
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 停止所有服务
stop_all() {
    print_status "停止所有服务..."
    
    # 停止前端
    if lsof -ti:$FRONTEND_PORT > /dev/null 2>&1; then
        lsof -ti:$FRONTEND_PORT | xargs kill -9 2>/dev/null || true
        print_status "前端已停止 (port $FRONTEND_PORT)"
    fi
    
    # 停止 Java 后端
    pkill -9 -f "java.*aihub" 2>/dev/null || true
    print_status "Java 后端已停止"
    
    # 停止 Python AI
    pkill -9 -f "uvicorn.*aihub" 2>/dev/null || true
    print_status "Python AI 已停止"
    
    print_status "所有服务已停止"
}

# 查看服务状态
status() {
    echo ""
    echo "=== AIHub 服务状态 ==="
    echo ""
    
    # 前端
    if lsof -ti:$FRONTEND_PORT > /dev/null 2>&1; then
        echo -e "前端:   ${GREEN}运行中${NC} (http://localhost:$FRONTEND_PORT)"
    else
        echo -e "前端:   ${RED}未运行${NC}"
    fi
    
    # Java 后端
    if lsof -ti:$JAVA_PORT > /dev/null 2>&1; then
        echo -e "Java:  ${GREEN}运行中${NC} (http://localhost:$JAVA_PORT)"
    else
        echo -e "Java:  ${RED}未运行${NC}"
    fi
    
    # Python AI
    if lsof -ti:$PYTHON_PORT > /dev/null 2>&1; then
        echo -e "Python:${GREEN}运行中${NC} (http://localhost:$PYTHON_PORT)"
    else
        echo -e "Python:${RED}未运行${NC}"
    fi
    
    echo ""
}

# 启动前端
start_frontend() {
    print_status "启动前端 (port $FRONTEND_PORT)..."
    cd "$PROJECT_ROOT/frontend"
    nohup pnpm dev --port $FRONTEND_PORT > "$LOG_DIR/frontend.log" 2>&1 &
    sleep 3
    
    if lsof -ti:$FRONTEND_PORT > /dev/null 2>&1; then
        print_status "前端启动成功: http://localhost:$FRONTEND_PORT"
    else
        print_error "前端启动失败，请查看日志: $LOG_DIR/frontend.log"
    fi
}

# 启动 Java 后端
start_java() {
    print_status "启动 Java 后端 (port $JAVA_PORT)..."
    
    # 先检查 Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装"
        exit 1
    fi
    
    cd "$PROJECT_ROOT/aihub-java/aihub-api"
    nohup mvn spring-boot:run -DskipTests > "$LOG_DIR/java.log" 2>&1 &
    
    # 等待服务启动
    print_status "等待 Java 后端启动..."
    for i in {1..30}; do
        if curl -s "http://localhost:$JAVA_PORT" > /dev/null 2>&1 || \
           curl -s "http://localhost:$JAVA_PORT/api" > /dev/null 2>&1; then
            print_status "Java 后端启动成功: http://localhost:$JAVA_PORT"
            return 0
        fi
        sleep 1
    done
    
    print_error "Java 后端启动失败，请查看日志: $LOG_DIR/java.log"
    return 1
}

# 启动 Python AI
start_python() {
    print_status "启动 Python AI (port $PYTHON_PORT)..."
    
    cd "$PROJECT_ROOT/aihub-python"
    
    # 检查虚拟环境
    if [ -d ".venv" ]; then
        source .venv/bin/activate
    fi
    
    # 启动服务
    nohup python -m uvicorn aihub.main:app \
        --host 0.0.0.0 \
        --port $PYTHON_PORT \
        > "$LOG_DIR/python.log" 2>&1 &
    
    # 等待服务启动
    print_status "等待 Python AI 启动..."
    for i in {1..15}; do
        if curl -s "http://localhost:$PYTHON_PORT/health" > /dev/null 2>&1; then
            print_status "Python AI 启动成功: http://localhost:$PYTHON_PORT"
            return 0
        fi
        sleep 1
    done
    
    print_warning "Python AI 可能启动失败，请查看日志: $LOG_DIR/python.log"
    return 0
}

# 主函数
main() {
    case "${1:-all}" in
        "all")
            print_status "启动所有 AIHub 服务..."
            print_status "端口: 前端=$FRONTEND_PORT, Java=$JAVA_PORT, Python=$PYTHON_PORT"
            echo ""
            
            stop_all
            echo ""
            
            start_java &
            start_python &
            
            wait
            sleep 2
            
            start_frontend
            
            echo ""
            status
            ;;
        "java")
            start_java
            ;;
        "python")
            start_python
            ;;
        "frontend")
            start_frontend
            ;;
        "stop")
            stop_all
            ;;
        "status")
            status
            ;;
        *)
            echo "用法: $0 {all|java|python|frontend|stop|status}"
            exit 1
            ;;
    esac
}

main "$@"
