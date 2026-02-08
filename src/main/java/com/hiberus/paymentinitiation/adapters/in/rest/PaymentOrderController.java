
package com.hiberus.paymentinitiation.adapters.in.rest;

import com.hiberus.paymentinitiation.domain.ports.in.InitiatePaymentOrderUseCase;
import com.hiberus.paymentinitiation.generated.api.PaymentOrderApi;
import com.hiberus.paymentinitiation.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.paymentinitiation.generated.model.PaymentOrderStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import static com.hiberus.paymentinitiation.adapters.in.rest.RestMapper.unwrap;

@RestController
@Validated
public class PaymentOrderController implements PaymentOrderApi {

  private final InitiatePaymentOrderUseCase initiateUseCase;
  private final RestMapper mapper;
  private final QueryFacade query;

  public PaymentOrderController(InitiatePaymentOrderUseCase initiateUseCase,
                                RestMapper mapper,
                                QueryFacade query) {
    this.initiateUseCase = initiateUseCase;
    this.mapper = mapper;
    this.query = query;
  }

    @Override
    public Mono<ResponseEntity<com.hiberus.paymentinitiation.generated.model.PaymentOrder>> initiatePaymentOrder(
            Mono<PaymentOrderInitiationRequest> paymentOrderInitiationRequest,
            String idempotencyKey,
            ServerWebExchange exchange) {

        return paymentOrderInitiationRequest.map(body -> {

            var ca = body.getCreditorAccount();
            String creditorBic     = unwrap(ca.getBankBic());
            String reference       = unwrap(body.getReference());
            LocalDate requestedDate= unwrap(body.getRequestedExecutionDate());
            String remittanceInfo  = unwrap(body.getRemittanceInfo());

            var po = initiateUseCase.initiate(
                    body.getDebtorAccountId(),
                    ca.getName(),
                    ca.getAccountId(),
                    creditorBic,
                    body.getAmount(),
                    body.getCurrency(),
                    reference,
                    requestedDate,
                    remittanceInfo,
                    idempotencyKey
            );

            var location = URI.create("/payment-initiation/payment-orders/" + po.id());
            return ResponseEntity.created(location).body(mapper.toApi(po));

        });
    }

  @Override
  public Mono<ResponseEntity<com.hiberus.paymentinitiation.generated.model.PaymentOrder>> retrievePaymentOrder(
          java.util.UUID id, ServerWebExchange exchange) {
    return Mono.fromSupplier(() -> query.get((id)))
        .map(po -> ResponseEntity.ok(mapper.toApi(po)))
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }


    public Mono<ResponseEntity<com.hiberus.paymentinitiation.generated.model.PaymentOrder>> retrievePaymentOrder(
            String id, ServerWebExchange exchange) {
        return retrievePaymentOrder(UUID.fromString(id), exchange);
    }

  @Override
  public Mono<ResponseEntity<PaymentOrderStatus>> retrievePaymentOrderStatus(java.util.UUID id, ServerWebExchange exchange) {
    return Mono.fromSupplier(() -> query.get((id)))
        .map(po -> ResponseEntity.ok(mapper.toApiStatus(po)));
  }


    public Mono<ResponseEntity<PaymentOrderStatus>> retrievePaymentOrderStatus(String id, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> query.get(UUID.fromString(id)))
                .map(po -> ResponseEntity.ok(mapper.toApiStatus(po)));
    }
}
