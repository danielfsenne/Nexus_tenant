# syntax=docker/dockerfile:1
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código-fonte, para reaproveitar
# o cache da camada quando só o código mudar (não o pom.xml).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S nexus && adduser -S nexus -G nexus
COPY --from=build /app/target/*.jar app.jar
USER nexus

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
