package com.hiberus.paymentinitiation.domain;

import com.hiberus.paymentinitiation.domain.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class PaymentOrderTest {

    @Test
    void createsWithInitiatedStatus_andGeneratesIdAndTimestamp() {
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10.0, "USD"),
                "ref",
                LocalDate.now(),
                "remit"
        );
        assertThat(po.id()).isNotNull();
        assertThat(po.createdAt()).isNotNull();
        assertThat(po.status()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(po.debtorAccountId()).isEqualTo("001-123");
        assertThat(po.creditorAccount().name()).isEqualTo("Juan");
        assertThat(po.instructedAmount().currency()).isEqualTo("USD");
    }

    @Test
    void failsFast_whenAmountIsZeroOrNegative() {
        assertThatThrownBy(() -> PaymentOrder.create(
                "001-123", new CreditorAccount("Juan","001-999",null),
                new Money(0.0,"USD"), null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullGuards_areEnforced() {
        assertThatThrownBy(() -> PaymentOrder.create(
                null, new CreditorAccount("Juan","001-999",null),
                new Money(1.0,"USD"), null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createOrder_and_markAsRejected(){
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10.0, "USD"),
                "ref",
                LocalDate.now(),
                "remit"
        );

        assertThat(po.id()).isNotNull();
        assertThat(po.status()).isEqualTo(PaymentStatus.INITIATED);
        po.markRejected("Test-Rejection");
        assertThat(po.status()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void createOrder_and_markAsAccepted(){
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10.0, "USD"),
                "ref",
                LocalDate.now(),
                "remit"
        );

        assertThat(po.id()).isNotNull();
        assertThat(po.status()).isEqualTo(PaymentStatus.INITIATED);
        po.markAccepted();
        assertThat(po.status()).isEqualTo(PaymentStatus.ACCEPTED);
    }
}