FROM maven:3.9.12-eclipse-temurin-25-noble AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src src
RUN mvn clean package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:25.0.1_8-jre-alpine-3.23
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY src/main/resources/minio/public.crt /tmp/minio.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias minio -file /tmp/minio.crt

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
