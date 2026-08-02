package com.apu.asc.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RateLimiterFilter implements Filter {

  private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> strictBuckets = new ConcurrentHashMap<>();

  private Bucket createGeneralBucket() {
    Bandwidth limit =
        Bandwidth.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build();
    return Bucket.builder().addLimit(limit).build();
  }

  private Bucket createStrictBucket() {
    Bandwidth limit =
        Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build();
    return Bucket.builder().addLimit(limit).build();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String clientIp = httpRequest.getRemoteAddr();
    String ipKey = clientIp != null ? clientIp : "anonymous";
    String uri = httpRequest.getRequestURI();

    boolean isStrictEndpoint =
        uri != null
            && (uri.startsWith("/api/v1/auth/forgot-password")
                || uri.startsWith("/api/v1/auth/login")
                || uri.startsWith("/api/v1/auth/register"));

    Bucket bucket;
    if (isStrictEndpoint) {
      bucket = strictBuckets.computeIfAbsent(ipKey, k -> createStrictBucket());
    } else {
      bucket = generalBuckets.computeIfAbsent(ipKey, k -> createGeneralBucket());
    }

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(429);
      httpResponse.setContentType("application/json");
      httpResponse
          .getWriter()
          .write(
              "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please wait a moment before trying again.\"}");
    }
  }
}
