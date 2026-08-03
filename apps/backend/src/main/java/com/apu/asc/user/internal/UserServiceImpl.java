package com.apu.asc.user.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.user.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserApi {

  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final KeycloakAdminService keycloakAdminService;
  private final EmailService emailService;
  private final InvitationTokenService invitationTokenService;

  @Value("${invitations.expiry-hours:48}")
  private long invitationExpiryHours;

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAllUsers() {
    return userRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserById(String id) {
    return userRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findById(String id) {
    return userRepository.findById(id).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByEmail(String email) {
    return userRepository.findByEmail(email).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByUsername(String username) {
    return userRepository.findByUsername(username).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByKeycloakId(String keycloakId) {
    return userRepository.findByKeycloakId(keycloakId).map(this::toDto);
  }

  @Override
  @Transactional
  public UserDto createUser(UserDto userDto) {
    if (userDto.username() != null
        && userRepository.findByUsername(userDto.username()).isPresent()) {
      throw new IllegalArgumentException(
          "Username '" + userDto.username() + "' is already registered.");
    }

    if (userDto.email() != null && userRepository.findByEmail(userDto.email()).isPresent()) {
      throw new IllegalArgumentException(
          "Email address '" + userDto.email() + "' is already registered.");
    }

    String generatedId =
        userDto.id() != null ? userDto.id() : "USR-" + UUID.randomUUID().toString();

    String userRole = userDto.role() != null ? userDto.role() : "CUSTOMER";
    boolean requiresEmployeeInvitation =
        "STAFF".equalsIgnoreCase(userRole) || "TECHNICIAN".equalsIgnoreCase(userRole);
    String userStatus =
        requiresEmployeeInvitation
            ? UserStatus.PENDING_VERIFICATION.name()
            : userDto.status() == null || userDto.status().isBlank()
                ? UserStatus.ACTIVE.name()
                : UserStatus.fromString(userDto.status()).name();
    InvitationTokenService.InvitationToken invitation =
        requiresEmployeeInvitation ? invitationTokenService.issue() : null;

    // Provision user in Keycloak if not provided
    String keycloakId = userDto.keycloakId();
    if (keycloakId == null || keycloakId.isBlank()) {
      keycloakId =
          keycloakAdminService.createKeycloakUser(
              userDto.username(),
              userDto.email(),
              userDto.fullName(),
              userRole,
              !requiresEmployeeInvitation);
    }

    UserEntity entity =
        UserEntity.builder()
            .id(generatedId)
            .keycloakId(keycloakId)
            .username(userDto.username())
            .email(userDto.email())
            .fullName(userDto.fullName())
            .role(userRole)
            .status(userStatus)
            .avatarUrl(userDto.avatarUrl())
            .invitationTokenHash(invitation != null ? invitation.tokenHash() : null)
            .invitationExpiresAt(
                invitation != null ? Instant.now().plusSeconds(invitationExpiryHours * 3600) : null)
            .build();

    UserDto created = toDto(userRepository.save(entity));

    if (invitation != null) {
      emailService.sendWelcomeInviteEmail(
          created.email(), created.username(), created.role(), invitation.rawToken());
    }

    eventPublisher.publishEvent(
        new AuditEvent(
            created.id(),
            "USER_CREATED",
            "USER",
            created.id(),
            "Created user with role: " + created.role()));
    return created;
  }

  @Override
  @Transactional
  public UserDto updateUser(String id, UserDto userDto) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

    if (userDto.fullName() != null) entity.setFullName(userDto.fullName());
    if (userDto.email() != null) entity.setEmail(userDto.email());
    if (userDto.role() != null) entity.setRole(userDto.role());
    if (userDto.avatarUrl() != null) entity.setAvatarUrl(userDto.avatarUrl());
    if (userDto.status() != null) {
      transitionStatus(entity, UserStatus.fromString(userDto.status()));
    }

    UserDto updated = toDto(userRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            id, "USER_UPDATED", "USER", id, "Updated user metadata for " + updated.email()));
    return updated;
  }

  @Override
  @Transactional
  public UserDto updateStatus(String id, String status) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    transitionStatus(entity, UserStatus.fromString(status));
    UserDto updated = toDto(userRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(id, "USER_STATUS_UPDATED", "USER", id, "Updated status to " + status));
    return updated;
  }

  @Override
  @Transactional
  public UserDto reissueEmployeeInvitation(String id) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    if (!requiresEmployeeInvitation(entity.getRole())) {
      throw new IllegalArgumentException(
          "Only staff and technician accounts can receive invitations.");
    }
    if (UserStatus.fromString(entity.getStatus()) == UserStatus.INACTIVE) {
      throw new IllegalArgumentException(
          "Reactivate the employee account before reissuing an invitation.");
    }

    InvitationTokenService.InvitationToken invitation = invitationTokenService.issue();
    entity.setStatus(UserStatus.PENDING_VERIFICATION.name());
    entity.setInvitationTokenHash(invitation.tokenHash());
    entity.setInvitationExpiresAt(Instant.now().plusSeconds(invitationExpiryHours * 3600));
    entity.setInvitationAcceptedAt(null);
    if (entity.getKeycloakId() != null) {
      keycloakAdminService.disableKeycloakUser(entity.getKeycloakId());
    }

    UserDto updated = toDto(userRepository.save(entity));
    emailService.sendWelcomeInviteEmail(
        updated.email(), updated.username(), updated.role(), invitation.rawToken());
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.id(),
            "EMPLOYEE_INVITATION_REISSUED",
            "USER",
            updated.id(),
            "Manager reissued employee account invitation."));
    return updated;
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    transitionStatus(entity, UserStatus.INACTIVE);
    userRepository.save(entity);
    eventPublisher.publishEvent(
        new AuditEvent(id, "USER_DEACTIVATED", "USER", id, "Deactivated user account"));
  }

  @Override
  @Transactional
  public void hardDeleteUser(String id) {
    userRepository
        .findById(id)
        .ifPresent(
            user -> {
              if (user.getKeycloakId() != null) {
                keycloakAdminService.deleteKeycloakUser(user.getKeycloakId());
              }
              userRepository.delete(user);
            });
  }

  @Override
  @Transactional
  public UserDto syncJitUser(
      String keycloakId, String username, String email, String fullName, String role) {
    Optional<UserEntity> existing = userRepository.findByKeycloakId(keycloakId);
    if (existing.isPresent()) {
      UserEntity user = existing.get();
      boolean updated = false;
      if (email != null && !email.equals(user.getEmail())) {
        user.setEmail(email);
        updated = true;
      }
      if (fullName != null && !fullName.equals(user.getFullName())) {
        user.setFullName(fullName);
        updated = true;
      }
      if (role != null && !role.equals(user.getRole())) {
        user.setRole(role);
        updated = true;
      }
      if (updated) {
        userRepository.save(user);
        eventPublisher.publishEvent(
            new AuditEvent(
                user.getId(),
                "USER_JIT_UPDATED",
                "USER",
                user.getId(),
                "Updated JIT user profile"));
      }
      return toDto(user);
    }

    String generatedId = "USR-" + UUID.randomUUID().toString();
    UserEntity newEntity =
        UserEntity.builder()
            .id(generatedId)
            .keycloakId(keycloakId)
            .username(username != null ? username : keycloakId)
            .email(email != null ? email : username + "@apu-asc.com")
            .fullName(fullName != null ? fullName : username)
            .role(role != null ? role : "CUSTOMER")
            .status("ACTIVE")
            .build();
    UserDto created = toDto(userRepository.save(newEntity));
    eventPublisher.publishEvent(
        new AuditEvent(
            created.id(), "USER_JIT_PROVISIONED", "USER", created.id(), "JIT Provisioned user"));
    return created;
  }

  private UserDto toDto(UserEntity entity) {
    return new UserDto(
        entity.getId(),
        entity.getKeycloakId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getFullName(),
        entity.getRole(),
        entity.getStatus(),
        entity.getAvatarUrl(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private void transitionStatus(UserEntity entity, UserStatus target) {
    UserStatus current = UserStatus.fromString(entity.getStatus());
    if (requiresEmployeeInvitation(entity.getRole())
        && current == UserStatus.PENDING_VERIFICATION
        && target == UserStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Employee accounts must be activated through their invitation link.");
    }
    current.requireTransitionTo(target);
    if (current == target) {
      return;
    }
    entity.setStatus(target.name());
    if (entity.getKeycloakId() == null) {
      return;
    }
    if (target == UserStatus.INACTIVE) {
      keycloakAdminService.disableKeycloakUser(entity.getKeycloakId());
    } else if (target == UserStatus.ACTIVE) {
      keycloakAdminService.enableKeycloakUser(entity.getKeycloakId());
    }
  }

  private boolean requiresEmployeeInvitation(String role) {
    return "STAFF".equalsIgnoreCase(role) || "TECHNICIAN".equalsIgnoreCase(role);
  }
}
