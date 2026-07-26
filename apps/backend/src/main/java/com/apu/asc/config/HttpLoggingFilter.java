package com.apu.asc.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {

  private static final String RESET = "\u001B[0m";
  private static final String GREEN = "\u001B[32m";
  private static final String YELLOW = "\u001B[33m";
  private static final String RED = "\u001B[31m";
  private static final String CYAN = "\u001B[36m";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long startTime = System.currentTimeMillis();
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullPath = (queryString != null) ? uri + "?" + queryString : uri;

    // Skip verbose pre-flight OPTIONS logging and actuator noise
    if ("OPTIONS".equalsIgnoreCase(method) || uri.startsWith("/actuator")) {
      filterChain.doFilter(request, response);
      return;
    }

    log.info("→ {}{}{} {}", CYAN, method, RESET, fullPath);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int status = response.getStatus();

      String statusColor = GREEN;
      if (status >= 500) {
        statusColor = RED;
      } else if (status >= 400) {
        statusColor = YELLOW;
      }

      String msg =
          String.format(
              "← %s%s%s %s %s%d%s (%dms)",
              CYAN, method, RESET, fullPath, statusColor, status, RESET, duration);

      if (status >= 500) {
        log.error("{}", msg);
      } else if (status >= 400) {
        log.warn("{}", msg);
      } else {
        log.info("{}", msg);
      }
    }
  }
}
