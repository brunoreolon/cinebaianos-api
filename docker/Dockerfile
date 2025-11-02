FROM eclipse-temurin:21.0.8_9-jdk-jammy AS builder

WORKDIR /app

COPY pom.xml mvnw ./

COPY .mvn .mvn

COPY src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine as run

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]