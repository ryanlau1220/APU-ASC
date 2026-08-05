package com.apu.asc.notification.internal;

import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.notification.NotificationApi;
import com.apu.asc.notification.NotificationDto;
import com.apu.asc.notification.NotificationRequestedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class NotificationServiceImpl implements NotificationApi {

  private final NotificationRepository notificationRepository;

  @Override
  @Transactional
  public NotificationDto create(NotificationRequestedEvent event, String recipientUserId) {
    return notificationRepository
        .findByRecipientUserIdAndEventId(recipientUserId, event.eventId())
        .map(this::toDto)
        .orElseGet(
            () ->
                toDto(
                    notificationRepository.save(
                        NotificationEntity.builder()
                            .id("NTF-" + UUID.randomUUID())
                            .eventId(event.eventId())
                            .recipientUserId(recipientUserId)
                            .type(event.type())
                            .title(event.title())
                            .body(event.body())
                            .link(event.link())
                            .createdAt(event.occurredAt())
                            .build())));
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationDto> findForRecipient(String recipientUserId, int limit) {
    return notificationRepository
        .findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId, PageRequest.of(0, limit))
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnread(String recipientUserId) {
    return notificationRepository.countByRecipientUserIdAndReadAtIsNull(recipientUserId);
  }

  @Override
  @Transactional
  public NotificationDto markRead(String id, String recipientUserId) {
    NotificationEntity notification =
        notificationRepository
            .findByIdAndRecipientUserId(id, recipientUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    notification.markRead(Instant.now());
    return toDto(notification);
  }

  @Override
  @Transactional
  public int markAllRead(String recipientUserId) {
    return notificationRepository.markAllRead(recipientUserId, Instant.now());
  }

  private NotificationDto toDto(NotificationEntity entity) {
    return new NotificationDto(
        entity.getId(),
        entity.getType(),
        entity.getTitle(),
        entity.getBody(),
        entity.getLink(),
        entity.getReadAt(),
        entity.getCreatedAt());
  }
}
