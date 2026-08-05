package com.apu.asc.notification.internal;

import com.apu.asc.notification.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "notifications",
    indexes =
        @Index(
            name = "idx_notifications_recipient_created",
            columnList = "recipient_user_id, created_at"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NotificationEntity {

  @Id private String id;

  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "recipient_user_id", nullable = false)
  private String recipientUserId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 64)
  private NotificationType type;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, length = 500)
  private String body;

  @Column(length = 512)
  private String link;

  private Instant readAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  void markRead(Instant readAt) {
    if (this.readAt == null) {
      this.readAt = readAt;
    }
  }
}
