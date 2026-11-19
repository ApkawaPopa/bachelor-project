FROM maven:3-eclipse-temurin-25-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src src
RUN mvn clean package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:25-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]