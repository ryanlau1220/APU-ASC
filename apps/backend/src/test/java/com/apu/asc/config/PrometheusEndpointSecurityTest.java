package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class PrometheusEndpointSecurityTest {

  @Test
  @DisplayName("Allows Prometheus scrapes only from the configured Docker network")
  void allowsScrapeFromConfiguredDockerNetwork() {
    SecurityConfig securityConfig = securityConfigFor("127.0.0.1/32,172.18.0.0/16");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("172.18.0.25");

    assertThat(securityConfig.prometheusScrapeAccessDecision(request).isGranted()).isTrue();
  }

  @Test
  @DisplayName("Allows Docker Desktop loopback-proxied Prometheus scrapes")
  void allowsDockerDesktopLoopbackScrape() {
    SecurityConfig securityConfig = securityConfigFor("127.0.0.1/32,172.18.0.0/16");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");

    assertThat(securityConfig.prometheusScrapeAccessDecision(request).isGranted()).isTrue();
  }

  @Test
  @DisplayName("Rejects Prometheus requests outside the configured Docker network")
  void rejectsScrapeOutsideConfiguredDockerNetwork() {
    SecurityConfig securityConfig = securityConfigFor("127.0.0.1/32,172.18.0.0/16");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("10.0.0.25");

    assertThat(securityConfig.prometheusScrapeAccessDecision(request).isGranted()).isFalse();
  }

  private SecurityConfig securityConfigFor(String allowedCidr) {
    SecurityConfig securityConfig = new SecurityConfig(null, null);
    ReflectionTestUtils.setField(securityConfig, "prometheusAllowedCidrs", allowedCidr);
    return securityConfig;
  }
}
