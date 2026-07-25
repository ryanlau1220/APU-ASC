package com.apu.asc.common.exception;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<ValidationErrorResponse.FieldErrorItem> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                err ->
                    new ValidationErrorResponse.FieldErrorItem(
                        err.getField(), err.getRejectedValue(), err.getDefaultMessage()))
            .toList();

    ValidationErrorResponse response =
        new ValidationErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed for request parameters",
            errors);

    return ResponseEntity.badRequest().body(response);
  }
}
