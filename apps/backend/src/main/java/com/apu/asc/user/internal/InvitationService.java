package com.apu.asc.user.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.user.UserStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class InvitationService {

  private static final String INVALID_INVITATION_MESSAGE = "Invalid or expired invitation token.";

  private final UserRepository userRepository;
  private final InvitationTokenService invitationTokenService;
  private final KeycloakAdminService keycloakAdminService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  InvitationActivationResult activate(String token, String newPassword) {
    UserEntity user =
        userRepository
            .findByInvitationTokenHashForUpdate(invitationTokenService.hash(token))
            .orElseThrow(() -> new IllegalArgumentException(INVALID_INVITATION_MESSAGE));

    Instant now = Instant.now();
    if (user.getInvitationAcceptedAt() != null
        || user.getInvitationExpiresAt() == null
        || !user.getInvitationExpiresAt().isAfter(now)
        || UserStatus.fromString(user.getStatus()) != UserStatus.PENDING_VERIFICATION
        || user.getKeycloakId() == null) {
      throw new IllegalArgumentException(INVALID_INVITATION_MESSAGE);
    }

    keycloakAdminService.resetUserPassword(user.getKeycloakId(), newPassword);
    keycloakAdminService.enableKeycloakUser(user.getKeycloakId());

    UserStatus.fromString(user.getStatus()).requireTransitionTo(UserStatus.ACTIVE);
    user.setStatus(UserStatus.ACTIVE.name());
    user.setInvitationTokenHash(null);
    user.setInvitationExpiresAt(null);
    user.setInvitationAcceptedAt(now);
    userRepository.save(user);

    eventPublisher.publishEvent(
        new AuditEvent(
            user.getId(),
            "EMPLOYEE_INVITATION_ACCEPTED",
            "USER",
            user.getId(),
            "Employee completed secure invitation password setup."));

    return new InvitationActivationResult(
        true, "Account activated successfully. You may now sign in.");
  }

  record InvitationActivationResult(boolean success, String message) {}
}
