package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.model.*;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class RestMapperTest {

    RestMapper mapper = new RestMapper();

    @Test
    void mapsDomainToApiModel() {
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999","BICXECU1"),
                new Money(25.5,"USD"),
                "Factura-1",
                LocalDate.now(),
                "Pago"
        );

        var api = mapper.toApi(po);

        assertThat(api.getId()).isEqualTo(po.id());
        assertThat(api.getCreatedAt()).isNotNull();
        assertThat(api.getDebtorAccountId()).isEqualTo("001-123");
        assertThat(api.getCreditorAccount().getName()).isEqualTo("Juan");
        assertThat(api.getCreditorAccount().getBankBic().orElse(null)).isEqualTo("BICXECU1");
        assertThat(api.getInstructedAmount().getAmount()).isEqualTo(25.5);
        assertThat(api.getReference().orElse(null)).isEqualTo("Factura-1");
    }

    @Test
    void mapsDomainToApiModel_whenOptionalsAreNull_setsUndefinedJsonNullable() {
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999", null), // bankBic null
                new Money(10,"USD"),
                null,             // reference null
                null,             // requestedExecutionDate null
                null              // remittanceInfo null
        );

        var api = mapper.toApi(po);

        // bankBic, reference, requestedExecutionDate y remittanceInfo => undefined (no presentes)
        assertThat(api.getCreditorAccount().getBankBic().isPresent()).isFalse();
        assertThat(api.getReference().isPresent()).isFalse();
        assertThat(api.getRequestedExecutionDate().isPresent()).isFalse();
        assertThat(api.getRemittanceInfo().isPresent()).isFalse();

        // createdAt mapeado a OffsetDateTime
        assertThat(api.getCreatedAt()).isNotNull();
    }


}