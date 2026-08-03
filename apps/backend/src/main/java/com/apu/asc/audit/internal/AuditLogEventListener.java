package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.common.event.AuditEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
class AuditLogEventListener {

  private final AuditLogApi auditLogApi;
  private final Counter auditWriteSuccessCounter;
  private final Counter auditWriteFailureCounter;

  AuditLogEventListener(AuditLogApi auditLogApi, MeterRegistry meterRegistry) {
    this.auditLogApi = auditLogApi;
    this.auditWriteSuccessCounter =
        Counter.builder("audit.log.write.success").register(meterRegistry);
    this.auditWriteFailureCounter =
        Counter.builder("audit.log.write.failure").register(meterRegistry);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void handleAuditEvent(AuditEvent event) {
    try {
      log.info(
          "[AUDIT EVENT] User: {}, Action: {}, Entity: {}, ID: {}",
          event.userId(),
          event.actionType(),
          event.entityName(),
          event.entityId());
      auditLogApi.logAction(event);
      auditWriteSuccessCounter.increment();
    } catch (Exception e) {
      log.warn(
          "Could not persist audit log for action: {} entity: {} ({})",
          event.actionType(),
          event.entityName(),
          e.getMessage());
      auditWriteFailureCounter.increment();
    }
  }
}
