
package com.hiberus.paymentinitiation.config;

import com.hiberus.paymentinitiation.adapters.out.memory.InMemoryPaymentOrderRepository;
import com.hiberus.paymentinitiation.application.InitiatePaymentOrderService;
import com.hiberus.paymentinitiation.domain.ports.in.InitiatePaymentOrderUseCase;
import com.hiberus.paymentinitiation.domain.ports.out.PaymentOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  @Bean
  public PaymentOrderRepository paymentOrderRepository() {
    return new InMemoryPaymentOrderRepository();
  }

  @Bean
  public InitiatePaymentOrderUseCase initiatePaymentOrderUseCase(PaymentOrderRepository repository) {
    return new InitiatePaymentOrderService(repository);
  }
}
