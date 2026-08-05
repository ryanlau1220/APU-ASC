package com.apu.asc.notification.internal;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.notification.NotificationApi;
import com.apu.asc.notification.NotificationRequestedEvent;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class NotificationEventListener {

  private final NotificationApi notificationApi;
  private final UserApi userApi;
  private final ApplicationEventPublisher eventPublisher;
  private final Counter createdNotifications;

  NotificationEventListener(
      NotificationApi notificationApi,
      UserApi userApi,
      ApplicationEventPublisher eventPublisher,
      MeterRegistry meterRegistry) {
    this.notificationApi = notificationApi;
    this.userApi = userApi;
    this.eventPublisher = eventPublisher;
    this.createdNotifications =
        Counter.builder("notification.delivery.created")
            .description("Persisted in-app notifications")
            .register(meterRegistry);
  }

  @ApplicationModuleListener(id = "notification-delivery")
  void handle(NotificationRequestedEvent event) {
    Set<String> recipients = new LinkedHashSet<>(event.recipientUserIds());
    if (!event.recipientRoles().isEmpty()) {
      userApi.findAllUsers().stream()
          .filter(user -> "ACTIVE".equalsIgnoreCase(user.status()))
          .filter(user -> event.recipientRoles().contains(normalizeRole(user)))
          .map(UserDto::id)
          .forEach(recipients::add);
    }

    recipients.removeIf(recipientId -> !userApi.isInAppNotificationsEnabled(recipientId));
    recipients.forEach(recipientId -> notificationApi.create(event, recipientId));
    if (!recipients.isEmpty()) {
      eventPublisher.publishEvent(
          LiveUpdateEvent.forUsersAndRoles("notifications", null, recipients, Set.of()));
      createdNotifications.increment(recipients.size());
    }
  }

  private String normalizeRole(UserDto user) {
    return user.role() == null ? "" : user.role().trim().toUpperCase(java.util.Locale.ROOT);
  }
}
