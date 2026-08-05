package com.apu.asc.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.user.UserPreferencesDto;
import com.apu.asc.user.UserPreferencesUpdateRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private KeycloakAdminService keycloakAdminService;

  @Test
  void updatesValidatedPreferencesAndAuditsTheChange() {
    UserEntity user = UserEntity.builder().id("USR-1").timeZone("UTC").build();
    when(userRepository.findById("USR-1")).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);
    UserServiceImpl service =
        new UserServiceImpl(userRepository, eventPublisher, keycloakAdminService);

    UserPreferencesDto updated =
        service.updatePreferences(
            "USR-1", new UserPreferencesUpdateRequest("Asia/Singapore", false));

    assertThat(updated.timeZone()).isEqualTo("Asia/Singapore");
    assertThat(updated.inAppNotificationsEnabled()).isFalse();
    ArgumentCaptor<AuditEvent> auditEvent = ArgumentCaptor.forClass(AuditEvent.class);
    verify(eventPublisher).publishEvent(auditEvent.capture());
    assertThat(auditEvent.getValue().actionType()).isEqualTo("USER_PREFERENCES_UPDATED");
  }

  @Test
  void rejectsAnInvalidTimeZoneBeforePersisting() {
    UserEntity user = UserEntity.builder().id("USR-1").build();
    when(userRepository.findById("USR-1")).thenReturn(Optional.of(user));
    UserServiceImpl service =
        new UserServiceImpl(userRepository, eventPublisher, keycloakAdminService);

    assertThatThrownBy(
            () ->
                service.updatePreferences(
                    "USR-1", new UserPreferencesUpdateRequest("not/a-time-zone", true)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeZone must be a valid IANA time zone.");

    verify(userRepository, never()).save(user);
  }
}
