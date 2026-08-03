package com.apu.asc.user.internal;

import com.apu.asc.common.event.EmployeeInvitationRequestedEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.user.UserStatus;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Delivers employee invitations from the persistent post-commit event publication registry. */
@Component
@RequiredArgsConstructor
class EmployeeInvitationDeliveryListener {

  private final UserRepository userRepository;
  private final InvitationTokenService invitationTokenService;
  private final EmailService emailService;

  @Value("${invitations.expiry-hours:48}")
  private long invitationExpiryHours;

  @ApplicationModuleListener(id = "employee-invitation-email")
  void on(EmployeeInvitationRequestedEvent event) {
    UserEntity user =
        userRepository
            .findById(event.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", event.userId()));
    if (!requiresEmployeeInvitation(user.getRole())
        || UserStatus.fromString(user.getStatus()) != UserStatus.PENDING_VERIFICATION) {
      return;
    }

    InvitationTokenService.InvitationToken invitation = invitationTokenService.issue();
    user.setInvitationTokenHash(invitation.tokenHash());
    user.setInvitationExpiresAt(Instant.now().plusSeconds(invitationExpiryHours * 3600));
    user.setInvitationAcceptedAt(null);
    userRepository.save(user);

    emailService.sendWelcomeInviteEmail(
        user.getEmail(), user.getUsername(), user.getRole(), invitation.rawToken());
  }

  private boolean requiresEmployeeInvitation(String role) {
    return "STAFF".equalsIgnoreCase(role) || "TECHNICIAN".equalsIgnoreCase(role);
  }
}
