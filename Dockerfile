FROM eclipse-temurin:21-jre
# 컨테이너 기본 타임존이 UTC라 서버 로그/LocalDateTime.now() 가 UTC로 찍힘 - KST로 고정한다.
ENV TZ=Asia/Seoul
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]