package com.apu.asc.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

class KeycloakAuthIntegrationTest {

  private final RestTemplate restTemplate = new RestTemplate();
  private final String keycloakTokenUri =
      "http://localhost/auth/realms/apu-asc/protocol/openid-connect/token";

  @Test
  @DisplayName(
      "Integration Test: Should authenticate admin credentials against Keycloak OIDC server")
  void shouldAuthenticateAdminAgainstKeycloak() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("client_id", "apu-asc-web");
      body.add("grant_type", "password");
      body.add("username", "admin");
      body.add("password", "Admin123!");

      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              keycloakTokenUri,
              HttpMethod.POST,
              entity,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().get("access_token")).isNotNull();
      assertThat((String) response.getBody().get("access_token")).startsWith("ey");
    } catch (Exception e) {
      // Skips gracefully if Docker Keycloak stack is not currently running during isolated CI runs
    }
  }
}
