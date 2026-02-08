// src/main/java/com/hiberus/paymentinitiation/application/InitiatePaymentOrderService.java
package com.hiberus.paymentinitiation.application;

import com.hiberus.paymentinitiation.domain.model.*;
import com.hiberus.paymentinitiation.domain.ports.in.InitiatePaymentOrderUseCase;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;

import java.time.LocalDate;
import java.util.Objects;

public class InitiatePaymentOrderService implements InitiatePaymentOrderUseCase {

    private final PaymentOrderRepository repository;

    public InitiatePaymentOrderService(PaymentOrderRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public PaymentOrder initiate(String debtorAccountId, String creditorName, String creditorAccountId,
                                 String creditorBic, double amount, String currency, String reference, LocalDate requestedDate,
                                 String remittanceInfo, String idempotencyKey) {

        // 1) Si hay key, intenta devolver la orden existente (sin parsear UUID aquí)

        if (idempotencyKey != null) {
            var maybeId = repository.findIdByIdempotencyKey(idempotencyKey);
            if (maybeId.isPresent()) {
                return repository.findById(maybeId.get())
                        .orElseThrow(() -> new IllegalStateException("Idempotency key mapped but order not found: " + idempotencyKey));
            }
        }


        // 2) Crear y guardar nueva orden
        PaymentOrder po = PaymentOrder.create(
                debtorAccountId,
                new CreditorAccount(creditorName, creditorAccountId, creditorBic),
                new Money(amount, currency),
                reference,
                requestedDate,
                remittanceInfo
        );

        PaymentOrder saved = repository.save(po);

        // 3) Asociar idempotency key -> id
        if (idempotencyKey != null) {
            repository.saveIdempotencyKey(idempotencyKey, saved.id());
        }
        return saved;
    }
}