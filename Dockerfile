
# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app
COPY --from=build /app/target/payment-initiation-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
