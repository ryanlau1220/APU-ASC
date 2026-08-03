package com.apu.asc.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.apu.asc.user.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private UserApi userApi;
  @Mock private UserRepository userRepository;
  @Mock private OtpCacheService otpCacheService;
  @Mock private EmailService emailService;
  @Mock private KeycloakAdminService keycloakAdminService;
  @Mock private InvitationService invitationService;
  @Mock private RestTemplate restTemplate;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController =
        new AuthController(
            userApi,
            userRepository,
            otpCacheService,
            emailService,
            keycloakAdminService,
            invitationService,
            restTemplate);
  }

  @Test
  @DisplayName("Should send OTP email when requested for valid email")
  void shouldSendForgotPasswordOtp() {
    when(keycloakAdminService.findUserIdByEmail("test@apu-asc.com")).thenReturn("KC-100");

    ResponseEntity<?> response =
        authController.requestForgotPasswordOtp(
            new AuthController.ForgotPasswordOtpRequest("test@apu-asc.com"), null);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  @DisplayName("Should reset password with valid OTP successfully")
  void shouldResetPasswordWithOtpSuccessfully() {
    when(otpCacheService.validateAndRemoveOtp("test@apu-asc.com", "123456")).thenReturn(true);
    when(keycloakAdminService.findUserIdByEmail("test@apu-asc.com")).thenReturn("KC-100");

    ResponseEntity<?> response =
        authController.resetPasswordWithOtp(
            new AuthController.ResetPasswordOtpRequest(
                "test@apu-asc.com", "123456", "newPassword123", "newPassword123"));

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  @DisplayName("Should activate an employee account with a valid invitation token")
  void shouldActivateInvitation() {
    String token = "a".repeat(43);
    when(invitationService.activate(token, "newPassword123"))
        .thenReturn(
            new InvitationService.InvitationActivationResult(
                true, "Account activated successfully. You may now sign in."));

    ResponseEntity<AuthController.InvitationActivationResponse> response =
        authController.activateInvitation(
            new AuthController.InvitationActivationRequest(
                token, "newPassword123", "newPassword123"));

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().success()).isTrue();
  }

  @Test
  @DisplayName("Should reject invitation activation when passwords differ")
  void shouldRejectMismatchedInvitationPasswords() {
    ResponseEntity<AuthController.InvitationActivationResponse> response =
        authController.activateInvitation(
            new AuthController.InvitationActivationRequest(
                "a".repeat(43), "newPassword123", "differentPassword123"));

    assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().success()).isFalse();
  }
}
