
# IA - Decisiones y correcciones humanas

- Ajusté nomenclatura a **/payment-initiation/payment-orders** para alinear con BIAN SD/BQ.
- Normalicé estados a: INITIATED, PENDING, ACCEPTED, REJECTED, EXECUTED, FAILED.
- Añadí header **Idempotency-Key** opcional en `POST`.
- Apliqué **RFC7807** para errores con `ProblemDetail` de Spring.
- Simplifiqué idempotencia (usa mapa en memoria; si el header es UUID de una creación previa, devuelve la misma entidad).
- Elegí **WebFlux** y mapeo reactor (`Mono`) al implementar la interfaz generada.
- Tests con **WebTestClient** y cobertura fácil ≥80% (dominio + integración).
