
package com.hiberus.paymentinitiation.adapters.in.rest;

import java.util.UUID;

public class NotFoundException extends RuntimeException {
  public NotFoundException(UUID id) { super("PaymentOrder not found: " + id); }
}
