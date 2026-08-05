package com.apu.asc.notification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** A durable request to create one private inbox notification for every intended recipient. */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public record NotificationRequestedEvent(
    UUID eventId,
    NotificationType type,
    Set<String> recipientUserIds,
    Set<String> recipientRoles,
    String title,
    String body,
    String link,
    Instant occurredAt) {

  public NotificationRequestedEvent {
    eventId = eventId == null ? UUID.randomUUID() : eventId;
    if (type == null) {
      throw new IllegalArgumentException("A notification type is required.");
    }
    recipientUserIds = normalize(recipientUserIds, false);
    recipientRoles = normalize(recipientRoles, true);
    if (recipientUserIds.isEmpty() && recipientRoles.isEmpty()) {
      throw new IllegalArgumentException("A notification requires at least one recipient.");
    }
    title = requireText(title, "title", 160);
    body = requireText(body, "body", 500);
    if (link != null && (!link.startsWith("/") || link.startsWith("//"))) {
      throw new IllegalArgumentException("Notification links must be internal application paths.");
    }
    occurredAt = occurredAt == null ? Instant.now() : occurredAt;
  }

  public static NotificationRequestedEvent forUsers(
      NotificationType type,
      Collection<String> recipientUserIds,
      String title,
      String body,
      String link) {
    return new NotificationRequestedEvent(
        null, type, normalize(recipientUserIds, false), Set.of(), title, body, link, Instant.now());
  }

  public static NotificationRequestedEvent forRoles(
      NotificationType type,
      Collection<String> recipientRoles,
      String title,
      String body,
      String link) {
    return new NotificationRequestedEvent(
        null, type, Set.of(), normalize(recipientRoles, true), title, body, link, Instant.now());
  }

  private static String requireText(String value, String field, int maxLength) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Notification " + field + " is required.");
    }
    String normalized = value.trim();
    if (normalized.length() > maxLength) {
      throw new IllegalArgumentException("Notification " + field + " is too long.");
    }
    return normalized;
  }

  private static Set<String> normalize(Collection<String> values, boolean uppercase) {
    if (values == null) {
      return Set.of();
    }
    return values.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(value -> uppercase ? value.toUpperCase(Locale.ROOT) : value)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }
}
