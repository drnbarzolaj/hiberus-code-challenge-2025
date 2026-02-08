
# IA - Prompts usados

1) **WSDL → Resumen y mapeo BIAN**
```
Resume este WSDL de órdenes de pago, lista operaciones/elementos clave y sugiere mapeo a BIAN Payment Initiation / BQ: PaymentOrder (recursos, estados, atributos mínimos), indicando qué campos SOAP no pasan a REST.
```

2) **Contrato OpenAPI (borrador)**
```
Genera un contrato OpenAPI 3.0 para PaymentOrder con POST (initiate) y GET (retrieve, retrieve-status), modelos con enum de estados y RFC7807.
```

3) **Esqueleto Hexagonal**
```
Crea estructura hexagonal (dominio, puertos, adaptadores) para Spring Boot 3 con WebFlux, repositorio en memoria e interfaces generadas por openapi-generator.
```

4) **Pruebas**
```
Genera tests unitarios para el dominio y una prueba de integración con WebTestClient que cubra POST y GETs.
```
