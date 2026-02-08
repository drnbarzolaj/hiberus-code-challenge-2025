package com.hiberus.paymentinitiation.application;

import com.hiberus.paymentinitiation.domain.model.*;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InitiatePaymentOrderServiceTest {

    PaymentOrderRepository repo;
    InitiatePaymentOrderService service;

    @BeforeEach
    void setUp() {
        repo = mock(PaymentOrderRepository.class);
        service = new InitiatePaymentOrderService(repo);
    }

    @Test
    void returnsExistingWhenIdempotencyKeyIsPresent() {
        var existing = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999", null),
                new Money(10,"USD"),
                null, LocalDate.now(), null
        );
        var key = "ABC-opaque-key";

        // ---- STUBS: sólo con Opción A (findIdByIdempotencyKey + findById) ----
        when(repo.findIdByIdempotencyKey(eq(key))).thenReturn(Optional.of(existing.id()));
        when(repo.findById(eq(existing.id()))).thenReturn(Optional.of(existing));

        // (opcional) Por si algo cae a la rama de creación, evita NPE
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // ---- CALL ----
        var result = service.initiate(
                "001-123","Juan","001-999", null, 10,"USD",
                null, null, null, key
        );

        // ---- ASSERTS ----
        assertThat(result).isSameAs(existing);

        // ---- VERIFY: se tomó la rama idempotente y NO la de creación ----
        verify(repo).findIdByIdempotencyKey(eq(key));
        verify(repo).findById(eq(existing.id()));
        verify(repo, never()).save(any());
        verify(repo, never()).saveIdempotencyKey(anyString(), any(UUID.class));
    }

    @Test
    void savesNewWhenNoIdempotencyKey() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = service.initiate("001-123","Juan","001-999",null,
                10,"USD", null, null, null, null);

        assertThat(result.id()).isNotNull();
        verify(repo).save(any());
    }

    @Test
    void returnsExisting_whenIdempotencyKeyAlreadyMapped() {
        var existing = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999", null),
                new Money(10,"USD"), null, LocalDate.now(), null
        );
        var key = "ABC-opaque-key";

        when(repo.findIdByIdempotencyKey(key)).thenReturn(Optional.of(existing.id()));
        when(repo.findById(existing.id())).thenReturn(Optional.of(existing));

        var result = service.initiate(
                "001-123","Juan","001-999", null, 10,"USD",
                null, null, null, key
        );

        assertThat(result).isSameAs(existing);
        verify(repo, never()).save(any());
    }

    @Test
    void createsNew_andStoresIdempotencyMapping_whenKeyNotMapped() {
        when(repo.findIdByIdempotencyKey("KEY-1")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.initiate(
                "001-123","Juan","001-999", null, 10,"USD",
                null, null, null, "KEY-1"
        );

        assertThat(result.id()).isNotNull();
        verify(repo).saveIdempotencyKey(eq("KEY-1"), eq(result.id()));
    }

    @Test
    void throwsIllegalState_whenIdempotencyKeyMappedButOrderNotFound() {
        var key = "ORPHAN-KEY";
        var orphanId = UUID.randomUUID();

        when(repo.findIdByIdempotencyKey(key)).thenReturn(Optional.of(orphanId));
        when(repo.findById(orphanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.initiate(
                "001-123", "Juan", "001-999", null, 10, "USD",
                null, null, null, key
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Idempotency key mapped but order not found")
                .hasMessageContaining(key);

        verify(repo).findIdByIdempotencyKey(key);
        verify(repo).findById(orphanId);
        verify(repo, never()).save(any());
    }
}