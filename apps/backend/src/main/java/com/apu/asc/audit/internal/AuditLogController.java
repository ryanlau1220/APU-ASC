package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.audit.AuditLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "System Audit Logs", description = "System operation and audit log APIs")
class AuditLogController {

  private final AuditLogApi auditLogApi;

  @GetMapping
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Get system audit logs")
  public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
    return ResponseEntity.ok(auditLogApi.findAllAuditLogs());
  }
}
