FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

# 시간대를 Asia/Seoul(KST)로 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

COPY build/libs/*.jar app.jar

EXPOSE 8080

# JVM 시간대도 명시적으로 설정
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]