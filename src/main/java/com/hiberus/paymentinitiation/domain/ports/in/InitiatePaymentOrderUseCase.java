
package com.hiberus.paymentinitiation.domain.ports.in;

import com.hiberus.paymentinitiation.domain.model.PaymentOrder;
import java.time.LocalDate;

public interface InitiatePaymentOrderUseCase {
  PaymentOrder initiate(String debtorAccountId, String creditorName, String creditorAccountId, String creditorBic,
                        double amount, String currency, String reference, LocalDate requestedDate,
                        String remittanceInfo, String idempotencyKey);
}
