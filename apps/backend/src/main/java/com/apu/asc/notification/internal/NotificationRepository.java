package com.apu.asc.notification.internal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
  Optional<NotificationEntity> findByRecipientUserIdAndEventId(
      String recipientUserId, UUID eventId);

  Optional<NotificationEntity> findByIdAndRecipientUserId(String id, String recipientUserId);

  List<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(
      String recipientUserId, Pageable pageable);

  long countByRecipientUserIdAndReadAtIsNull(String recipientUserId);

  @Modifying(clearAutomatically = true)
  @Query(
      "update NotificationEntity notification set notification.readAt = :readAt "
          + "where notification.recipientUserId = :recipientUserId and notification.readAt is null")
  int markAllRead(
      @Param("recipientUserId") String recipientUserId, @Param("readAt") Instant readAt);
}
