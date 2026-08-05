package com.apu.asc.notification;

import java.time.Instant;

public record NotificationDto(
    String id,
    NotificationType type,
    String title,
    String body,
    String link,
    Instant readAt,
    Instant createdAt) {}
