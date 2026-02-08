package com.hiberus.paymentinitiation.adapters.out.memory;

import com.hiberus.paymentinitiation.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPaymentOrderRepositoryTest {

    @Test
    void save_find_idempotency() {
        var repo = new InMemoryPaymentOrderRepository();

        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan","001-999", null),
                new Money(25.5,"USD"),
                "ref", LocalDate.now(), "remit"
        );

        repo.save(po);
        assertThat(repo.findById(po.id())).isPresent();

        repo.saveIdempotencyKey("KEY-1", po.id());
        assertThat(repo.existsByIdempotencyKey("KEY-1")).isTrue();
    }


    @Test
    void findIdByIdempotencyKey_returnsPresent_whenMapped_andEmpty_whenNotMapped() {
        var repo = new InMemoryPaymentOrderRepository();

        // Creamos y guardamos una orden
        var po = PaymentOrder.create(
                "001-123",
                new CreditorAccount("Juan", "001-999", null),
                new Money(10, "USD"),
                null, LocalDate.now(), null
        );
        repo.save(po);

        // Mapeamos una key -> id (CUBRE camino "no-null")
        repo.saveIdempotencyKey("KEY-1", po.id());

        // 1) Key mapeada => Optional presente con el id correcto
        var present = repo.findIdByIdempotencyKey("KEY-1");
        assertThat(present).isPresent();
        assertThat(present.get()).isEqualTo(po.id());

        // 2) Key NO mapeada => Optional vacío (CUBRE camino "null")
        var empty = repo.findIdByIdempotencyKey("MISSING");
        assertThat(empty).isEmpty();
    }

}