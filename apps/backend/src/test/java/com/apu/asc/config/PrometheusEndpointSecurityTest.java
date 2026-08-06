package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
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

  @Test
  @DisplayName(
      "Accepts the local Android Keycloak issuer alias without broadening production trust")
  void acceptsExplicitAdditionalJwtIssuer() {
    SecurityConfig securityConfig = securityConfigFor("127.0.0.1/32");
    ReflectionTestUtils.setField(
        securityConfig, "jwtIssuerUri", "http://localhost/auth/realms/apu-asc");
    ReflectionTestUtils.setField(
        securityConfig, "additionalJwtIssuerUris", "http://localhost:8080/auth/realms/apu-asc");

    assertThat(
            securityConfig
                .trustedIssuerValidator()
                .validate(jwtFrom("http://localhost:8080/auth/realms/apu-asc"))
                .hasErrors())
        .isFalse();
    assertThat(
            securityConfig
                .trustedIssuerValidator()
                .validate(jwtFrom("https://untrusted.example/realms/apu-asc"))
                .hasErrors())
        .isTrue();
  }

  private SecurityConfig securityConfigFor(String allowedCidr) {
    SecurityConfig securityConfig = new SecurityConfig(null, null);
    ReflectionTestUtils.setField(securityConfig, "prometheusAllowedCidrs", allowedCidr);
    return securityConfig;
  }

  private Jwt jwtFrom(String issuer) {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .issuer(issuer)
        .subject("user")
        .issuedAt(java.time.Instant.now())
        .expiresAt(java.time.Instant.now().plusSeconds(60))
        .build();
  }
}
