# 多阶段构建：构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build-stage

WORKDIR /app

# 复制父 pom.xml（构建上下文是项目根目录）
COPY backend/pom.xml ./parent-pom.xml

# 复制当前模块目录
COPY backend/aihub-api/pom.xml ./aihub-api/pom.xml
COPY backend/aihub-api/src ./aihub-api/src

# 下载依赖（利用 Docker 缓存层）
# 先安装父 pom，然后下载当前模块依赖
WORKDIR /app/aihub-api
RUN mvn install -f ../parent-pom.xml -N -B && \
    mvn dependency:go-offline -B

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 从构建阶段复制 jar 文件
COPY --from=build-stage --chown=spring:spring /app/aihub-api/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
