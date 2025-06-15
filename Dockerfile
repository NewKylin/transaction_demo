# 使用 JDK 21 轻量级运行时镜像
FROM eclipse-temurin:21-jre-alpine

# 设置时区（可选）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 复制应用
COPY target/transaction-app.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数（启用 ZGC、优化容器内存）
ENV JAVA_OPTS="-XX:+UseZGC \
  -Xms2g \
  -Xmx2g \
  -XX:InitialRAMPercentage=50.0 \
  -XX:MaxRAMPercentage=80.0 \
  -XX:ConcGCThreads=2 \
  -XX:ParallelGCThreads=4 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/heapdump.hprof"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]