package com.apu.asc.notification.internal;

import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.notification.NotificationApi;
import com.apu.asc.notification.NotificationDto;
import com.apu.asc.notification.NotificationUnreadCountDto;
import com.apu.asc.user.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Private in-app notification inbox")
class NotificationController {

  private final NotificationApi notificationApi;
  private final CurrentUserService currentUserService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(operationId = "getMyNotifications", summary = "Get notifications for the current user")
  ResponseEntity<List<NotificationDto>> getMyNotifications(
      Authentication authentication,
      @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
    AuthenticatedUser user = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(notificationApi.findForRecipient(user.id(), limit));
  }

  @GetMapping("/unread-count")
  @PreAuthorize("isAuthenticated()")
  @Operation(operationId = "getUnreadNotificationCount", summary = "Get unread notification count")
  ResponseEntity<NotificationUnreadCountDto> getUnreadCount(Authentication authentication) {
    AuthenticatedUser user = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(
        new NotificationUnreadCountDto(notificationApi.countUnread(user.id())));
  }

  @PatchMapping("/{id}/read")
  @PreAuthorize("isAuthenticated()")
  @Operation(operationId = "markNotificationRead", summary = "Mark one notification as read")
  ResponseEntity<NotificationDto> markRead(@PathVariable String id, Authentication authentication) {
    AuthenticatedUser user = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(notificationApi.markRead(id, user.id()));
  }

  @PostMapping("/read-all")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      operationId = "markAllNotificationsRead",
      summary = "Mark all current-user notifications as read")
  ResponseEntity<Void> markAllRead(Authentication authentication) {
    AuthenticatedUser user = currentUserService.requireCurrentUser(authentication);
    notificationApi.markAllRead(user.id());
    return ResponseEntity.noContent().build();
  }
}
