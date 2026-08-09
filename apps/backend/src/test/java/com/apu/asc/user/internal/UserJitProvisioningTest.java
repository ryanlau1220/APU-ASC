package com.apu.asc.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UserJitProvisioningTest {

  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private KeycloakAdminService keycloakAdminService;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userRepository, eventPublisher, keycloakAdminService);
  }

  @Test
  void relinksAnExistingAccountOnlyWhenUsernameAndEmailMatch() {
    UserEntity existing =
        UserEntity.builder()
            .id("USR-001")
            .keycloakId("stale-keycloak-subject")
            .username("admin")
            .email("admin@apu-asc.com")
            .fullName("System Manager")
            .role("MANAGER")
            .status("ACTIVE")
            .build();
    when(userRepository.findByKeycloakId("current-keycloak-subject")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existing));
    when(userRepository.findByEmail("admin@apu-asc.com")).thenReturn(Optional.of(existing));
    when(userRepository.save(existing)).thenReturn(existing);

    userService.syncJitUser(
        "current-keycloak-subject", "admin", "admin@apu-asc.com", "System Manager", "MANAGER");

    assertThat(existing.getKeycloakId()).isEqualTo("current-keycloak-subject");
    verify(userRepository).save(existing);
    verify(eventPublisher).publishEvent(any(Object.class));
  }

  @Test
  void rejectsAConflictingUsernameAndEmailInsteadOfLinkingAccounts() {
    UserEntity usernameOwner =
        UserEntity.builder()
            .id("USR-001")
            .username("admin")
            .email("admin@apu-asc.com")
            .fullName("Admin")
            .role("MANAGER")
            .status("ACTIVE")
            .build();
    UserEntity emailOwner =
        UserEntity.builder()
            .id("USR-002")
            .username("different-admin")
            .email("other@apu-asc.com")
            .fullName("Other Admin")
            .role("MANAGER")
            .status("ACTIVE")
            .build();
    when(userRepository.findByKeycloakId("current-keycloak-subject")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(usernameOwner));
    when(userRepository.findByEmail("admin@apu-asc.com")).thenReturn(Optional.of(emailOwner));

    assertThatThrownBy(
            () ->
                userService.syncJitUser(
                    "current-keycloak-subject", "admin", "admin@apu-asc.com", "Admin", "MANAGER"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("different application account");
  }
}
