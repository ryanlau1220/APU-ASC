package com.apu.asc.notification.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock private NotificationRepository notificationRepository;

  @Test
  void doesNotRevealOrModifyAnotherUsersNotification() {
    NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository);
    when(notificationRepository.findByIdAndRecipientUserId("NTF-1", "USR-2"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.markRead("NTF-1", "USR-2"))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(notificationRepository).findByIdAndRecipientUserId("NTF-1", "USR-2");
  }
}
