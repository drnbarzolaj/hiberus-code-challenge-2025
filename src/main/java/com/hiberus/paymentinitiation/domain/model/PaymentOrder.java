package com.hiberus.paymentinitiation.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class PaymentOrder {

    private final UUID id;
    private PaymentStatus status;
    private final Instant createdAt;
    private final String debtorAccountId;
    private final CreditorAccount creditorAccount;
    private final Money instructedAmount;
    private final String reference;
    private final LocalDate requestedExecutionDate;
    private final String remittanceInfo;

    // 1) Constructor PRIVADO: solo asigna, NO valida, NO lanza
    private PaymentOrder(UUID id, String debtorAccountId, CreditorAccount creditorAccount,
                         Money instructedAmount, String reference, LocalDate requestedExecutionDate,
                         String remittanceInfo) {
        this.id = id;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = Instant.now();
        this.debtorAccountId = debtorAccountId;
        this.creditorAccount = creditorAccount;
        this.instructedAmount = instructedAmount;
        this.reference = reference;
        this.requestedExecutionDate = requestedExecutionDate;
        this.remittanceInfo = remittanceInfo;
    }

    // 2) Fábrica ESTÁTICA: valida TODO ANTES del new (si algo falla, el ctor NI SE EJECUTA)
    public static PaymentOrder create(String debtorAccountId, CreditorAccount creditorAccount,
                                      Money instructedAmount, String reference,
                                      LocalDate requestedExecutionDate, String remittanceInfo) {

        Objects.requireNonNull(debtorAccountId, "debtorAccountId");
        Objects.requireNonNull(creditorAccount, "creditorAccount");
        Objects.requireNonNull(instructedAmount, "instructedAmount");
        if (instructedAmount.amount() <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        UUID id = UUID.randomUUID();
        return new PaymentOrder(
                id, debtorAccountId, creditorAccount, instructedAmount,
                reference, requestedExecutionDate, remittanceInfo
        );
    }

    // getters (sin cambios)
    public UUID id() { return id; }
    public PaymentStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public String debtorAccountId() { return debtorAccountId; }
    public CreditorAccount creditorAccount() { return creditorAccount; }
    public Money instructedAmount() { return instructedAmount; }
    public String reference() { return reference; }
    public LocalDate requestedExecutionDate() { return requestedExecutionDate; }
    public String remittanceInfo() { return remittanceInfo; }

    public void markAccepted() { this.status = PaymentStatus.ACCEPTED; }
    public void markRejected(String reason) { this.status = PaymentStatus.REJECTED; }
}