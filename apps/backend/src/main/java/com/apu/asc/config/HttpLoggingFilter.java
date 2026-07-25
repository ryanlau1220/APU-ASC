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

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long startTime = System.currentTimeMillis();
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String remoteAddr = request.getRemoteAddr();

    log.info("[HTTP IN] {} {} from {}", method, uri, remoteAddr);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int status = response.getStatus();
      log.info("[HTTP OUT] {} {} -> Status {} (Took {}ms)", method, uri, status, duration);
    }
  }
}
