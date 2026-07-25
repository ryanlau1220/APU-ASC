package com.apu.asc.user.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication & Registration",
    description = "Customer self-registration and universal login")
@Slf4j
class AuthController {

  private final UserApi userApi;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value(
      "${spring.security.oauth2.resourceserver.jwt.token-uri:http://localhost/auth/realms/apu-asc/protocol/openid-connect/token}")
  private String keycloakTokenUri;

  @PostMapping("/register-customer")
  @Operation(
      summary = "Register customer account",
      description = "Registers a new customer account requiring Keycloak confirmation")
  public ResponseEntity<UserDto> registerCustomer(
      @Valid @RequestBody final RegisterCustomerRequest request) {
    UserDto newUser =
        new UserDto(
            null,
            null,
            request.username(),
            request.email(),
            request.fullName(),
            request.contactNumber(),
            "CUSTOMER",
            "PENDING_VERIFICATION",
            Instant.now(),
            Instant.now());
    return ResponseEntity.ok(userApi.createUser(newUser));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Login endpoint",
      description = "Authenticates user against Keycloak OIDC and returns access token")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
    log.info("Attempting Keycloak authentication for user: {}", request.identifier());

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("client_id", "apu-asc-web");
      body.add("grant_type", "password");
      body.add("username", request.identifier());
      body.add("password", request.password());

      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              keycloakTokenUri,
              HttpMethod.POST,
              entity,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> responseMap = response.getBody();
      if (response.getStatusCode().is2xxSuccessful() && responseMap != null) {
        Object tokenObj = responseMap.get("access_token");
        if (tokenObj != null) {
          String accessToken = tokenObj.toString();
          log.info("Keycloak authentication successful for user: {}", request.identifier());
          return ResponseEntity.ok(
              new LoginResponse(
                  true, "Authentication successful", request.identifier(), accessToken));
        }
      }
    } catch (Exception e) {
      log.warn(
          "Keycloak authentication failed for user {}: {}", request.identifier(), e.getMessage());
    }

    return ResponseEntity.status(401)
        .body(new LoginResponse(false, "Invalid username or password", request.identifier(), null));
  }

  public record RegisterCustomerRequest(
      @NotBlank(message = "Username is required") String username,
      @NotBlank(message = "Email is required") String email,
      @NotBlank(message = "Full name is required") String fullName,
      @NotBlank(message = "Contact number is required") String contactNumber,
      @NotBlank(message = "Password is required") String password) {}

  public record LoginRequest(
      @NotBlank(message = "Username or Email is required") String identifier,
      @NotBlank(message = "Password is required") String password) {}

  public record LoginResponse(boolean success, String message, String username, String token) {}
}
