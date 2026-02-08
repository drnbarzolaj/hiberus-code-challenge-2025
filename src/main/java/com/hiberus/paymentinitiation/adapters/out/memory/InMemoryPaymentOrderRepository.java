// src/main/java/com/hiberus/paymentinitiation/adapters/out/memory/InMemoryPaymentOrderRepository.java
package com.hiberus.paymentinitiation.adapters.out.memory;

import com.hiberus.paymentinitiation.domain.model.PaymentOrder;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentOrderRepository implements PaymentOrderRepository {
    private final Map<UUID, PaymentOrder> store = new ConcurrentHashMap<>();
    private final Map<String, UUID> idempotency = new ConcurrentHashMap<>();

    @Override public PaymentOrder save(PaymentOrder po) { store.put(po.id(), po); return po; }
    @Override public Optional<PaymentOrder> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public boolean existsByIdempotencyKey(String key) { return idempotency.containsKey(key); }
    @Override public void saveIdempotencyKey(String key, UUID id) { idempotency.put(key, id); }

    @Override
    public Optional<UUID> findIdByIdempotencyKey(String key) {
        return Optional.ofNullable(idempotency.get(key));
    }
}