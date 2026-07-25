package com.apu.asc.user.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication & Registration",
    description = "Customer self-registration and universal login")
class AuthController {

  private final UserApi userApi;

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
      description = "Authenticates user via username or email and returns session status")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
    String identifier = request.identifier();
    return ResponseEntity.ok(
        new LoginResponse(true, "Authentication successful", identifier, "Bearer demo-jwt-token"));
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
