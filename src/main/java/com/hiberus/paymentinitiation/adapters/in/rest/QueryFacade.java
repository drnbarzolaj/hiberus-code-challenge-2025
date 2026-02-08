
package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.model.PaymentOrder;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QueryFacade {
  private final PaymentOrderRepository repo;
  public QueryFacade(PaymentOrderRepository repo) { this.repo = repo; }
  public PaymentOrder get(UUID id) { return repo.findById(id).orElseThrow(() -> new NotFoundException(id)); }
}
