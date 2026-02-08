package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.model.PaymentOrder;
import com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class RestMapper {

    private static <T> JsonNullable<T> toJsonNullable(T value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }

    // Helper: NO usa get(); devuelve null si está undefined
    static <T> T unwrap(JsonNullable<T> v) {
        return v.orElse(null);
    }

    public com.hiberus.paymentinitiation.generated.model.PaymentOrder toApi(PaymentOrder po) {
        var api = new com.hiberus.paymentinitiation.generated.model.PaymentOrder();

        // Campos no-null
        api.setId(po.id()); // UUID
        api.setCreatedAt(OffsetDateTime.ofInstant(po.createdAt(), ZoneOffset.UTC));
        api.setStatus(toApiStatus(po));
        api.setDebtorAccountId(po.debtorAccountId());

        // CreditorAccount (bankBic es JsonNullable)
        var ca = new com.hiberus.paymentinitiation.generated.model.CreditorAccount();
        ca.setName(po.creditorAccount().name());
        ca.setAccountId(po.creditorAccount().accountId());
        ca.setBankBic(toJsonNullable(po.creditorAccount().bankBic())); // ✅ usa setter JsonNullable
        api.setCreditorAccount(ca);

        // InstructedAmount
        var amt = new com.hiberus.paymentinitiation.generated.model.InstructedAmount();
        amt.setAmount(po.instructedAmount().amount());
        amt.setCurrency(po.instructedAmount().currency());
        api.setInstructedAmount(amt);

        // ⚠️ Para los opcionales: usar SIEMPRE los setters de JsonNullable
        api.setReference(toJsonNullable(po.reference()));                         // ✅ evita fluent .reference(...)
        api.setRequestedExecutionDate(toJsonNullable(po.requestedExecutionDate())); // ✅ evita fluent .requestedExecutionDate(...)
        api.setRemittanceInfo(toJsonNullable(po.remittanceInfo()));               // ✅ evita fluent .remittanceInfo(...)

        return api;
    }

    public PaymentOrderStatus toApiStatus(PaymentOrder po) {
        var st = new PaymentOrderStatus();
        st.setCode(PaymentOrderStatus.CodeEnum.valueOf(po.status().name()));
        return st;
    }
}