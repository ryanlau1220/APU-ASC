package com.apu.asc.common.event;

import com.apu.asc.common.security.AuthenticatedUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A non-sensitive, post-commit UI invalidation signal for the intended application audience.
 *
 * <p>The event deliberately carries no business data. Clients refetch through their normal,
 * object-authorized APIs after receiving it.
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public record LiveUpdateEvent(
    String topic,
    String resourceId,
    Set<String> audienceUserIds,
    Set<String> audienceRoles,
    Instant occurredAt) {

  public LiveUpdateEvent {
    if (topic == null || topic.isBlank()) {
      throw new IllegalArgumentException("A live-update topic is required.");
    }
    topic = topic.trim();
    resourceId = resourceId == null || resourceId.isBlank() ? null : resourceId.trim();
    audienceUserIds = normalize(audienceUserIds, false);
    audienceRoles = normalize(audienceRoles, true);
    occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    if (audienceUserIds.isEmpty() && audienceRoles.isEmpty()) {
      throw new IllegalArgumentException("A live update must have an audience.");
    }
  }

  public static LiveUpdateEvent forUsersAndRoles(
      String topic, String resourceId, Collection<String> userIds, Collection<String> roles) {
    return new LiveUpdateEvent(
        topic, resourceId, normalize(userIds, false), normalize(roles, true), Instant.now());
  }

  public static LiveUpdateEvent forRoles(String topic, String resourceId, String... roles) {
    return forUsersAndRoles(topic, resourceId, Set.of(), Set.of(roles));
  }

  public boolean isVisibleTo(AuthenticatedUser user) {
    return audienceUserIds.contains(user.id())
        || user.roles().stream()
            .map(role -> role.toUpperCase(Locale.ROOT))
            .anyMatch(audienceRoles::contains);
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
