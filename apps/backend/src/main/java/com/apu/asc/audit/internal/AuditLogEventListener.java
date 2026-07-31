package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.common.event.AuditEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
class AuditLogEventListener {

  private final AuditLogApi auditLogApi;

  @Async
  @EventListener
  public void handleAuditEvent(AuditEvent event) {
    log.info(
        "[AUDIT EVENT] User: {}, Action: {}, Entity: {}, ID: {}",
        event.userId(),
        event.actionType(),
        event.entityName(),
        event.entityId());
    auditLogApi.logAction(
        event.userId(), event.actionType(), event.entityName(), event.entityId(), event.details());
  }
}
