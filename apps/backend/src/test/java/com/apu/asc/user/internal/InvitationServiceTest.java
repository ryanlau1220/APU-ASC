package com.apu.asc.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.apu.asc.common.event.AuditEvent;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private KeycloakAdminService keycloakAdminService;
  @Mock private ApplicationEventPublisher eventPublisher;

  private final InvitationTokenService invitationTokenService = new InvitationTokenService();
  private InvitationService invitationService;

  @BeforeEach
  void setUp() {
    invitationService =
        new InvitationService(
            userRepository, invitationTokenService, keycloakAdminService, eventPublisher);
  }

  @Test
  void consumesValidInvitationBeforeEnablingTheEmployeeAccount() {
    InvitationTokenService.InvitationToken invitation = invitationTokenService.issue();
    UserEntity user = pendingUser(invitation.tokenHash(), Instant.now().plusSeconds(3600));
    when(userRepository.findByInvitationTokenHashForUpdate(invitation.tokenHash()))
        .thenReturn(Optional.of(user));

    InvitationService.InvitationActivationResult result =
        invitationService.activate(invitation.rawToken(), "newPassword123");

    assertThat(result.success()).isTrue();
    assertThat(user.getStatus()).isEqualTo("ACTIVE");
    assertThat(user.getInvitationTokenHash()).isNull();
    assertThat(user.getInvitationExpiresAt()).isNull();
    assertThat(user.getInvitationAcceptedAt()).isNotNull();
    verify(keycloakAdminService).resetUserPassword("KC-001", "newPassword123");
    verify(keycloakAdminService).enableKeycloakUser("KC-001");
    verify(userRepository).save(user);
    verify(eventPublisher).publishEvent(any(AuditEvent.class));
  }

  @Test
  void rejectsExpiredInvitationWithoutChangingTheAccount() {
    InvitationTokenService.InvitationToken invitation = invitationTokenService.issue();
    when(userRepository.findByInvitationTokenHashForUpdate(invitation.tokenHash()))
        .thenReturn(
            Optional.of(pendingUser(invitation.tokenHash(), Instant.now().minusSeconds(1))));

    assertThatThrownBy(() -> invitationService.activate(invitation.rawToken(), "newPassword123"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid or expired invitation token.");

    verifyNoInteractions(keycloakAdminService, eventPublisher);
  }

  private UserEntity pendingUser(String tokenHash, Instant expiration) {
    return UserEntity.builder()
        .id("USR-001")
        .keycloakId("KC-001")
        .username("employee")
        .email("employee@example.com")
        .fullName("Employee Example")
        .role("STAFF")
        .status("PENDING_VERIFICATION")
        .invitationTokenHash(tokenHash)
        .invitationExpiresAt(expiration)
        .build();
  }
}
