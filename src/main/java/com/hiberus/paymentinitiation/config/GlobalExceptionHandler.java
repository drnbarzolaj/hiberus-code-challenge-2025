
package com.hiberus.paymentinitiation.config;

import com.hiberus.paymentinitiation.adapters.in.rest.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(java.net.URI.create("https://example.com/problems/not-found"));
    pd.setTitle("Resource not found");
    return pd;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleValidation(IllegalArgumentException ex) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(java.net.URI.create("https://example.com/problems/validation"));
    pd.setTitle("Validation error");
    return pd;
  }
}
