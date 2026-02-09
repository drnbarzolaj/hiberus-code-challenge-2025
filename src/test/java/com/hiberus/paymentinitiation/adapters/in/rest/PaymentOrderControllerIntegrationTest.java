package com.hiberus.paymentinitiation.adapters.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Prueba de integración "end-to-end" con WebTestClient.
 * Lanza la app en puerto aleatorio y valida el contrato HTTP de los endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PaymentOrderControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void post_initiatePaymentOrder_returns201_andLocation_then_getById_returns200() {
        // language=json
        String body = """
        {
          "debtorAccountId": "001-123456-01",
          "creditorAccount": { "name": "Juan", "accountId": "001-654321-99" },
          "amount": 25.5,
          "currency": "USD"
        }
        """;

        // POST -> 201  Location (URI del recurso creado)

        EntityExchangeResult<byte[]> postResult =
                webTestClient.post()
                        .uri("/payment-initiation/payment-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectHeader().valueMatches("Location", "^/payment-initiation/payment-orders/.+")
                        // Cierra la verificación del body de forma no reactiva y devuelve EntityExchangeResult
                        .expectBody()
                        .returnResult();


        var location = postResult.getResponseHeaders().getLocation();

        // GET (por Location) -> 200  cuerpo JSON con campos mínimos
        webTestClient.get()
                .uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.status").isNotEmpty();
    }
}