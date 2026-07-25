package com.apu.asc.common.exception;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
    Instant timestamp, int status, String error, String message, List<FieldErrorItem> fieldErrors) {

  public ValidationErrorResponse {
    fieldErrors = fieldErrors != null ? List.copyOf(fieldErrors) : List.of();
  }

  public record FieldErrorItem(String field, Object rejectedValue, String message) {}
}
