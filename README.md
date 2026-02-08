
# Payment Initiation / PaymentOrder (WebFlux + In-Memory)

Implementación de microservicio **Spring Boot 3 (Java 17)** para la migración de un servicio legado de **órdenes de pago** a **REST** con enfoque **contract-first (OpenAPI 3.0)** y **arquitectura hexagonal**.

- Endpoints clave: `POST /payment-initiation/payment-orders`, `GET /payment-initiation/payment-orders/{id}`, `GET /payment-initiation/payment-orders/{id}/status`.
- Stack: WebFlux, Validación, Actuator, Pruebas (JUnit5, WebTestClient), JaCoCo ≥80%, Checkstyle, SpotBugs, Docker.
- Persistencia: **en memoria** (adapter reemplazable por R2DBC si se desea).

> Basado en el enunciado de la prueba técnica (WSDL para análisis, BIAN Payment Initiation/PaymentOrder, contract-first, hexagonal, calidad y Docker). 

## Requisitos
- JDK 17+
- Maven 3.9+ (o usar IntelliJ con import Maven)
- Docker (opcional, para empaquetado/ejecución container)

## Cómo ejecutar (Windows / IntelliJ)

### Opción A) IntelliJ IDEA
1. **File → New → Project from Existing Sources...** y selecciona esta carpeta.
2. IntelliJ detectará Maven y descargará dependencias.
3. Abre la vista **Maven** y ejecuta: `Lifecycle → verify` (genera código desde OpenAPI, compila, tests, calidad).
4. Ejecuta la app: `PaymentInitiationApplication` (perfil default).

### Opción B) Línea de comandos
```powershell
mvn -v
mvn clean verify
mvn spring-boot:run
```

### Probar
```powershell
# Crear una orden
curl -X POST http://localhost:8080/payment-initiation/payment-orders ^
  -H "Content-Type: application/json" ^
  -d "{"debtorAccountId":"001-123456-01","creditorAccount":{"name":"Juan","accountId":"001-654321-99"},"amount":25.5,"currency":"USD"}"

# Consultar por id (reemplaza {id})
curl http://localhost:8080/payment-initiation/payment-orders/{id}

# Consultar status
curl http://localhost:8080/payment-initiation/payment-orders/{id}/status
```

## Calidad
- `mvn verify` ejecuta **JaCoCo** (falla si cobertura < 80%), **Checkstyle** y **SpotBugs**.

## Docker
```powershell
# Construir imagen
docker build -t payment-initiation:1.0.0 .
# Ejecutar con docker-compose
docker compose up --build
```

## Estructura
```
src/main/java/com/hiberus/paymentinitiation
  - config/ (wiring, handlers RFC7807)
  - domain/ (modelo + puertos)
  - application/ (casos de uso)
  - adapters/in/rest (controladores + mappers)
  - adapters/out/memory (repositorio en memoria)
```

## Evidencia de IA
Consulta la carpeta `/ai` con prompts, generaciones y decisiones humanas.
