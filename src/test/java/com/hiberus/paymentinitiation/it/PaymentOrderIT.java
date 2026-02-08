
package com.hiberus.paymentinitiation.it;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentOrderIT {

  @Autowired
  WebTestClient web;

  @Test
  void initiate_and_retrieve_order() {
    var req = Map.of(
      "debtorAccountId","001-123456-01",
      "creditorAccount", Map.of("name","Juan","accountId","001-654321-99"),
      "amount", 25.5,
      "currency","USD",
      "reference","Factura-7890"
    );

    final String[] idHolder = new String[1];

    web.post()
      .uri("/payment-initiation/payment-orders")
      .bodyValue(req)
      .exchange()
      .expectStatus().isCreated()
      .expectHeader().exists("Location")
      .expectBody()
      .jsonPath("$.id").value(v -> idHolder[0] = v.toString())
      .jsonPath("$.status.code").isEqualTo("INITIATED");

    // Retrieve by id
    web.get().uri("/payment-initiation/payment-orders/" + idHolder[0])
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.id").isEqualTo(idHolder[0]);

    // Retrieve status
    web.get().uri("/payment-initiation/payment-orders/" + idHolder[0] + "/status")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.code").isEqualTo("INITIATED");

    // Health
    web.get().uri("/actuator/health")
      .exchange()
      .expectStatus().isOk();
  }
}
