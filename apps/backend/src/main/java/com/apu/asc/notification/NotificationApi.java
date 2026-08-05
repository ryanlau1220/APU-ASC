package com.apu.asc.notification;

import java.util.List;

public interface NotificationApi {
  NotificationDto create(NotificationRequestedEvent event, String recipientUserId);

  List<NotificationDto> findForRecipient(String recipientUserId, int limit);

  long countUnread(String recipientUserId);

  NotificationDto markRead(String id, String recipientUserId);

  int markAllRead(String recipientUserId);
}
