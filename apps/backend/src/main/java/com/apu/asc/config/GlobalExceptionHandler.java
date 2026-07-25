package com.apu.asc.config;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler({IOException.class, AsyncRequestTimeoutException.class})
  public void handleClientDisconnect(Exception ex) {
    // Suppress noisy Tomcat broken pipe / client disconnect stack traces for SSE streams
    log.debug("[SSE/NETWORK] Client connection closed or timed out: {}", ex.getMessage());
  }
}
