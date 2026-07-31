package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimiterFilterTest {

  private RateLimiterFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RateLimiterFilter();
  }

  @Test
  @DisplayName("Should allow requests within rate limit")
  void testAllowsRequestsWithinLimit() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(request.getRemoteAddr()).thenReturn("192.168.1.10");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, chain);
    }

    verify(chain, times(10)).doFilter(request, response);
  }

  @Test
  @DisplayName("Should return 429 when rate limit is exceeded")
  void testExceedsRateLimit() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    when(request.getRemoteAddr()).thenReturn("192.168.1.99");
    when(response.getWriter()).thenReturn(pw);

    // Consume all 100 capacity
    for (int i = 0; i < 100; i++) {
      filter.doFilter(request, response, chain);
    }

    // 101st request should trigger 429
    filter.doFilter(request, response, chain);

    verify(response).setStatus(429);
    assertThat(sw.toString()).contains("Rate limit exceeded");
  }
}
