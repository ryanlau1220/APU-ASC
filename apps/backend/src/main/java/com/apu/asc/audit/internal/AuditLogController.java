package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.audit.AuditLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "System Audit Logs", description = "System operation and compliance audit log APIs")
class AuditLogController {

  private final AuditLogApi auditLogApi;

  @GetMapping
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Get system audit logs")
  public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
    return ResponseEntity.ok(auditLogApi.findAllAuditLogs());
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Get audit logs by user ID")
  public ResponseEntity<List<AuditLogDto>> getLogsByUser(@PathVariable final String userId) {
    return ResponseEntity.ok(auditLogApi.findByUserId(userId));
  }

  @GetMapping("/entity/{entityName}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Get audit logs by entity type")
  public ResponseEntity<List<AuditLogDto>> getLogsByEntity(@PathVariable final String entityName) {
    return ResponseEntity.ok(auditLogApi.findByEntityName(entityName));
  }

  @GetMapping("/action/{actionType}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Get audit logs by action type")
  public ResponseEntity<List<AuditLogDto>> getLogsByAction(@PathVariable final String actionType) {
    return ResponseEntity.ok(auditLogApi.findByActionType(actionType));
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my user audit log history")
  public ResponseEntity<List<AuditLogDto>> getMyAuditLogs(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.ok(List.of());
    }
    return ResponseEntity.ok(auditLogApi.findByUserId(authentication.getName()));
  }

  @GetMapping("/sentry-test")
  @Operation(summary = "Test Sentry backend exception capture")
  public ResponseEntity<String> testSentry() {
    io.sentry.Sentry.captureException(new RuntimeException("APU-ASC Sentry Test Exception"));
    return ResponseEntity.ok("Sentry test exception captured successfully");
  }
}
