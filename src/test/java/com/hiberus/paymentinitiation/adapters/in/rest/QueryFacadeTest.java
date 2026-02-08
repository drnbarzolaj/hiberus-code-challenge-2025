package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.model.CreditorAccount;
import com.hiberus.paymentinitiation.domain.model.Money;
import com.hiberus.paymentinitiation.domain.model.PaymentOrder;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class QueryFacadeTest {

    @Mock
    PaymentOrderRepository repo;
    @InjectMocks
    QueryFacade query;

    @Test
    void get_returnsPaymentOrder_whenFound() {
        var id = UUID.randomUUID();
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999", null),
                new Money(10,"USD"),
                null, LocalDate.now(), null
        );

        Mockito.when(repo.findById(id)).thenReturn(Optional.of(po));

        assertThat(query.get(id)).isEqualTo(po);
    }

    @Test
    void get_throwsNotFoundException_whenNotFound() {
        var id = UUID.randomUUID();
        Mockito.when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> query.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
