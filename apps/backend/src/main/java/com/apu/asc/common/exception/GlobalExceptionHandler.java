package com.apu.asc.common.exception;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(IOException.class)
  public void handleClientDisconnect(IOException ex) {
    // Suppress noisy Tomcat broken pipe / client disconnect stack traces for SSE streams
    log.debug("[SSE/NETWORK] Client connection closed: {}", ex.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
      AsyncRequestTimeoutException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest webRequest) {
    log.debug("[SSE/NETWORK] Async request timeout: {}", ex.getMessage());
    return null;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            status, "Validation failed for one or more request parameters");
    problemDetail.setType(URI.create("https://apu-asc.com/errors/validation-error"));
    problemDetail.setTitle("Validation Failure");

    List<FieldErrorParam> invalidParams =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                err ->
                    new FieldErrorParam(
                        err.getField(),
                        err.getRejectedValue() != null ? err.getRejectedValue().toString() : null,
                        err.getDefaultMessage()))
            .toList();

    problemDetail.setProperty("invalidParams", invalidParams);

    return this.createResponseEntity(problemDetail, headers, status, request);
  }

  public record FieldErrorParam(String name, String value, String reason) {}
}
