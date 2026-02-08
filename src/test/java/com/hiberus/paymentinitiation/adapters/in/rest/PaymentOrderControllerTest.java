package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.model.*;
import com.hiberus.paymentinitiation.domain.ports.in.InitiatePaymentOrderUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(controllers = PaymentOrderController.class)
@Import(PaymentOrderControllerTest.JacksonTestConfig.class)
class PaymentOrderControllerTest {

    @Autowired WebTestClient web;
    @MockBean InitiatePaymentOrderUseCase useCase;
    @MockBean RestMapper mapper;
    @MockBean QueryFacade query;


    @Test
    void post_initiatePaymentOrder_returns201() {
        // JSON válido (evita intricacias de JsonNullable en cliente de test)
        var reqJson = """
      {
        "debtorAccountId": "001-123",
        "creditorAccount": { "name": "Juan", "accountId": "001-999" },
        "amount": 10.0,
        "currency": "USD"
      }
      """;

        // Dominio simulado
        var po = com.hiberus.paymentinitiation.domain.model.PaymentOrder.create(
                "001-123",
                new com.hiberus.paymentinitiation.domain.model.CreditorAccount("Juan", "001-999", null),
                new com.hiberus.paymentinitiation.domain.model.Money(10, "USD"),
                null, java.time.LocalDate.now(), null
        );

        // Respuesta API simulada
        var api = new com.hiberus.paymentinitiation.generated.model.PaymentOrder()
                .id(po.id())
                .debtorAccountId("001-123")
                .status(new com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus()
                        .code(com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus.CodeEnum.INITIATED));

        // Stubs: firma EXACTA (ver punto 2)
        Mockito.when(useCase.initiate(
                anyString(), anyString(), anyString(),
                nullable(String.class), anyDouble(), anyString(),
                nullable(String.class), nullable(java.time.LocalDate.class),
                nullable(String.class), nullable(String.class)
        )).thenReturn(po);

        Mockito.when(mapper.toApi(po)).thenReturn(api);

        web.post().uri("/payment-initiation/payment-orders")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(reqJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(result -> {
                    System.out.println("RESPONSE: " + new String(result.getResponseBodyContent(), StandardCharsets.UTF_8));
                })
        ;
    }

    @Test
    void get_retrievePaymentOrder_returns200() {
        var id = UUID.randomUUID();
        var po = PaymentOrder.create("001-123",
                new com.hiberus.paymentinitiation.domain.model.CreditorAccount("Juan","001-999", null),
                new Money(10,"USD"), null, LocalDate.now(), null);
        var api = new com.hiberus.paymentinitiation.generated.model.PaymentOrder().id(po.id());
        Mockito.when(query.get(id)).thenReturn(po);
        Mockito.when(mapper.toApi(po)).thenReturn(api);

        web.get().uri("/payment-initiation/payment-orders/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(po.id().toString());
    }

    @Test
    void post_initiatePaymentOrder_returns400_whenAmountIsInvalid() {
        var badJson = """
    { "debtorAccountId":"001-123",
      "creditorAccount": { "name":"Juan", "accountId":"001-999" },
      "amount": 0.0, "currency":"USD" }""";

        // Simulamos que el caso de uso valida y lanza IllegalArgumentException
        Mockito.when(useCase.initiate(
                anyString(), anyString(), anyString(),
                nullable(String.class), anyDouble(), anyString(),
                nullable(String.class), nullable(LocalDate.class),
                nullable(String.class), nullable(String.class)
        )).thenThrow(new IllegalArgumentException("Amount must be > 0"));

        web.post().uri("/payment-initiation/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
    }

    @Test
    void get_retrievePaymentOrder_returns404_whenNotFound() {
        var id = UUID.randomUUID();
        Mockito.when(query.get(id)).thenThrow(new NotFoundException(id));

        web.get().uri("/payment-initiation/payment-orders/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void get_retrievePaymentOrder_StringId_returns404_whenNotFound() {
        var stringId = UUID.randomUUID();
        var controller = new PaymentOrderController(useCase, mapper, query);

        // Subscribe to the Mono and assert on the emitted ResponseEntity
        var result = controller.retrievePaymentOrder(stringId.toString(), null).block();

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                result.getStatusCode()
        );
        org.junit.jupiter.api.Assertions.assertNull(result.getBody());

    }

    @Test
    void get_retrievePaymentOrder_StringId_returns200_whenFound() {

        var id = UUID.randomUUID();
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10, "USD"),
                null, LocalDate.now(), null
        );

        Mockito.when(query.get(id)).thenReturn(po);

        // The String overload is invoked internally via UUID path variable parsing,
        // but we can also test the controller method directly
        var controller = new PaymentOrderController(useCase, mapper, query);
        var result = controller.retrievePaymentOrder(id.toString(), null).block();

        assert result != null;
        System.out.println(result);
        assert result.getStatusCode().equals(HttpStatus.OK);


    }

    @Test
    void get_retrievePaymentOrderStatus_returns200() {
        var id = UUID.randomUUID();
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10, "USD"),
                null, LocalDate.now(), null
        );

        var status = new com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus()
                .code(com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus.CodeEnum.INITIATED);

        Mockito.when(query.get(id)).thenReturn(po);
        Mockito.when(mapper.toApiStatus(po)).thenReturn(status);

        web.get().uri("/payment-initiation/payment-orders/{id}/status", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INITIATED");
    }

    @Test
    void post_initiatePaymentOrder_withIdempotencyKey_returns201() {
        var reqJson = """
                { "debtorAccountId":"001-123",
                  "creditorAccount": { "name":"Juan", "accountId":"001-999" },
                  "amount": 10.0, "currency":"USD" }""";

        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10, "USD"),
                null, LocalDate.now(), null
        );

        var api = new com.hiberus.paymentinitiation.generated.model.PaymentOrder()
                .id(po.id()).debtorAccountId("001-123");

        Mockito.when(useCase.initiate(
                anyString(), anyString(), anyString(),
                nullable(String.class), anyDouble(), anyString(),
                nullable(String.class), nullable(LocalDate.class),
                nullable(String.class), nullable(String.class)
        )).thenReturn(po);
        Mockito.when(mapper.toApi(po)).thenReturn(api);

        web.post().uri("/payment-initiation/payment-orders")
                .header("Idempotency-Key", "abc-123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reqJson)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void get_retrievePaymentOrderStatus_withStringId_returns200() {
        var id = UUID.randomUUID();
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10, "USD"),
                null, LocalDate.now(), null
        );

        var status = new com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus()
                .code(com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus.CodeEnum.INITIATED);

        Mockito.when(query.get(id)).thenReturn(po);
        Mockito.when(mapper.toApiStatus(po)).thenReturn(status);

        // The String overload is invoked internally via UUID path variable parsing,
        // but we can also test the controller method directly
        var controller = new PaymentOrderController(useCase, mapper, query);
        var result = controller.retrievePaymentOrderStatus(id.toString(), null).block();

        assert result != null;
        assert result.getStatusCode().is2xxSuccessful();
        assert result.getBody() != null;
        assert result.getBody().getCode() ==
                com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus.CodeEnum.INITIATED;
    }

    @Test
    void get_retrievePaymentOrderStatus_withInvalidStringId_throwsException() {
        var controller = new PaymentOrderController(useCase, mapper, query);


        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> controller.retrievePaymentOrderStatus("not-a-uuid", null).block()
        );
    }

    @TestConfiguration
    static class JacksonTestConfig {
        @Bean
        com.fasterxml.jackson.databind.Module jsonNullableModule() {
            return new org.openapitools.jackson.nullable.JsonNullableModule();
        }
    }

}