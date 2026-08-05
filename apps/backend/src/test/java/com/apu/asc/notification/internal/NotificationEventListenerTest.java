package com.apu.asc.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.notification.NotificationApi;
import com.apu.asc.notification.NotificationRequestedEvent;
import com.apu.asc.notification.NotificationType;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock private NotificationApi notificationApi;
  @Mock private UserApi userApi;
  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  void createsNotificationsOnlyForActiveAudienceMembersAndPublishesScopedRefresh() {
    when(userApi.findAllUsers())
        .thenReturn(
            List.of(
                user("USR-staff", "STAFF", "ACTIVE"),
                user("USR-inactive", "STAFF", "INACTIVE"),
                user("USR-customer", "CUSTOMER", "ACTIVE")));
    NotificationEventListener listener =
        new NotificationEventListener(
            notificationApi, userApi, eventPublisher, new SimpleMeterRegistry());
    NotificationRequestedEvent event =
        NotificationRequestedEvent.forRoles(
            NotificationType.QUOTATION_DECIDED,
            List.of("STAFF"),
            "Customer responded",
            "Quotation QT-100 was approved.",
            "/staff/quotations");

    listener.handle(event);

    verify(notificationApi).create(event, "USR-staff");
    verify(notificationApi, never()).create(any(), eq("USR-inactive"));
    ArgumentCaptor<LiveUpdateEvent> liveUpdate = ArgumentCaptor.forClass(LiveUpdateEvent.class);
    verify(eventPublisher).publishEvent(liveUpdate.capture());
    assertThat(liveUpdate.getValue().topic()).isEqualTo("notifications");
    assertThat(liveUpdate.getValue().audienceUserIds()).containsExactly("USR-staff");
  }

  private UserDto user(String id, String role, String status) {
    return new UserDto(
        id,
        null,
        id.toLowerCase(),
        id.toLowerCase() + "@apu-asc.test",
        id,
        role,
        status,
        null,
        Instant.now(),
        Instant.now());
  }
}
