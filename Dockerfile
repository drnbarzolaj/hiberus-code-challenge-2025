# ====== STAGE 1: BUILD ======
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copiamos POM primero para aprovechar cache de dependencias
COPY pom.xml .
# Descarga dependencias (sin compilar)
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Ahora sí, copiamos el resto del código
COPY src ./src

# Compilamos y empacamos (salida en target/*.jar)
RUN mvn -q -e -B -DskipTests clean package

# ====== STAGE 2: RUNTIME ======
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Crea usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia el JAR desde el stage de build
# Ajusta el nombre del jar si tu artefacto usa otro nombre
COPY --from=build /workspace/target/*SNAPSHOT*.jar /app/app.jar

# Exponer puerto (informativo)
EXPOSE 8080

# Variables de entorno útiles
ENV SPRING_PROFILES_ACTIVE=default
# Ajusta memoria y flags de GC si deseas
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Healthcheck simple a actuator (si tienes /actuator/health)
# Si no usas actuator, cámbialo a /payment-initiation/payment-orders (GET) o bórralo.
HEALTHCHECK --interval=20s --timeout=3s --start-period=20s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Ejecuta la app
ENTRYPOINT ["java","-jar","/app/app.jar"]