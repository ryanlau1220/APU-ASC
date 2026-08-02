package com.apu.asc.user.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.SecureRandom;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    description = "Customer self-registration, universal login, and password management")
@Slf4j
class AuthController {

  private final UserApi userApi;
  private final UserRepository userRepository;
  private final OtpCacheService otpCacheService;
  private final EmailService emailService;
  private final KeycloakAdminService keycloakAdminService;
  private final RestTemplate restTemplate;
  private final SecureRandom secureRandom = new SecureRandom();

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

  @PostMapping("/forgot-password/request")
  @Operation(
      summary = "Request password reset OTP",
      description = "Generates and emails a 6-digit OTP to the registered user")
  public ResponseEntity<Map<String, Object>> requestForgotPasswordOtp(
      @Valid @RequestBody final ForgotPasswordOtpRequest request,
      HttpServletRequest servletRequest) {
    String email = request.email().trim().toLowerCase();
    log.info("Received password reset OTP request for email: {}", email);

    // Verify user exists in DB or Keycloak
    String keycloakId = keycloakAdminService.findUserIdByEmail(email);
    if (keycloakId == null && userRepository.findByEmail(email).isEmpty()) {
      log.warn("Password reset requested for non-existent email: {}", email);
      return ResponseEntity.ok(
          Map.of(
              "success",
              true,
              "message",
              "If an account exists with this email, an OTP code has been sent.",
              "expiresInSeconds",
              600));
    }

    // Generate 6-digit numeric OTP code
    String otpCode = String.format("%06d", secureRandom.nextInt(1000000));
    otpCacheService.putOtp(email, otpCode);

    // Deliver OTP email
    emailService.sendOtpEmail(email, otpCode, servletRequest);

    return ResponseEntity.ok(
        Map.of(
            "success",
            true,
            "message",
            "OTP verification code sent to your email address.",
            "expiresInSeconds",
            600));
  }

  @PostMapping("/forgot-password/reset")
  @Operation(
      summary = "Reset password using OTP",
      description = "Verifies 6-digit OTP and resets user password in Keycloak")
  public ResponseEntity<Map<String, Object>> resetPasswordWithOtp(
      @Valid @RequestBody final ResetPasswordOtpRequest request) {
    String email = request.email().trim().toLowerCase();
    log.info("Attempting OTP password reset for email: {}", email);

    if (!request.newPassword().equals(request.confirmPassword())) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "success", false, "message", "New password and confirm password do not match."));
    }

    // Validate in-memory OTP
    if (!otpCacheService.validateAndRemoveOtp(email, request.otp())) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "Invalid or expired OTP code."));
    }

    // Find Keycloak user ID
    String keycloakId = keycloakAdminService.findUserIdByEmail(email);
    if (keycloakId == null) {
      var userOpt = userRepository.findByEmail(email);
      if (userOpt.isPresent() && userOpt.get().getKeycloakId() != null) {
        keycloakId = userOpt.get().getKeycloakId();
      }
    }

    if (keycloakId == null) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "Associated Keycloak user account not found."));
    }

    // Reset password in Keycloak
    keycloakAdminService.resetUserPassword(keycloakId, request.newPassword());

    return ResponseEntity.ok(
        Map.of(
            "success",
            true,
            "message",
            "Password reset successfully. You may now sign in with your new password."));
  }

  @PostMapping("/change-password")
  @Operation(
      summary = "Change password on profile page",
      description = "Allows logged in user to update password by providing old and new password")
  public ResponseEntity<Map<String, Object>> changePassword(
      @AuthenticationPrincipal OidcUser oidcUser,
      @Valid @RequestBody final ChangePasswordRequest request) {
    if (oidcUser == null) {
      return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
    }

    String username = oidcUser.getPreferredUsername();
    String email = oidcUser.getEmail();
    log.info("Attempting profile password change for user: {}", username);

    if (!request.newPassword().equals(request.confirmPassword())) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "success", false, "message", "New password and confirm password do not match."));
    }

    // Verify Old Password against Keycloak
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("client_id", "apu-asc-web");
      body.add("grant_type", "password");
      body.add("username", username);
      body.add("password", request.oldPassword());

      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              keycloakTokenUri,
              HttpMethod.POST,
              entity,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      if (!response.getStatusCode().is2xxSuccessful()) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "Current password is incorrect."));
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "Current password is incorrect."));
    }

    // Find Keycloak User ID and update password
    String keycloakId = oidcUser.getSubject();
    if (keycloakId == null) {
      keycloakId = keycloakAdminService.findUserIdByEmail(email);
    }

    if (keycloakId == null) {
      return ResponseEntity.badRequest()
          .body(Map.of("success", false, "message", "User account not found in Keycloak."));
    }

    keycloakAdminService.resetUserPassword(keycloakId, request.newPassword());

    return ResponseEntity.ok(
        Map.of("success", true, "message", "Password changed successfully. Please log in again."));
  }

  public record RegisterCustomerRequest(
      @NotBlank(message = "Username is required") String username,
      @NotBlank(message = "Email is required") String email,
      @NotBlank(message = "Full name is required") String fullName,
      @NotBlank(message = "Password is required") String password) {}

  public record LoginRequest(
      @NotBlank(message = "Username or Email is required") String identifier,
      @NotBlank(message = "Password is required") String password) {}

  public record ForgotPasswordOtpRequest(
      @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
          String email) {}

  public record ResetPasswordOtpRequest(
      @NotBlank(message = "Email is required") @Email String email,
      @NotBlank(message = "OTP code is required")
          @Size(min = 6, max = 6, message = "OTP must be 6 digits")
          String otp,
      @NotBlank(message = "New password is required")
          @Size(min = 8, message = "Password must be at least 8 characters")
          String newPassword,
      @NotBlank(message = "Confirm password is required") String confirmPassword) {}

  public record ChangePasswordRequest(
      @NotBlank(message = "Current password is required") String oldPassword,
      @NotBlank(message = "New password is required")
          @Size(min = 8, message = "Password must be at least 8 characters")
          String newPassword,
      @NotBlank(message = "Confirm password is required") String confirmPassword) {}

  public record LoginResponse(boolean success, String message, String username, String token) {}
}
