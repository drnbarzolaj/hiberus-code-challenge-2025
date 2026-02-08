// src/main/java/com/hiberus/paymentinitiation/domain/ports/out/PaymentOrderRepository.java
package com.hiberus.paymentinitiation.domain.ports.out;

import com.hiberus.paymentinitiation.domain.model.PaymentOrder;

import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder po);
    Optional<PaymentOrder> findById(UUID id);

    boolean existsByIdempotencyKey(String key);
    void saveIdempotencyKey(String key, UUID id);

    // 👇 nuevo: obtener el id asociado a la key (sin asumir formato)
    Optional<UUID> findIdByIdempotencyKey(String key);
}