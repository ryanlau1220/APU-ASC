package com.apu.asc.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditEventTest {

  @Test
  void capturesSystemContextAndPreservesSafeStateTransitions() {
    AuditEvent event =
        new AuditEvent(
            "USR-100",
            "USER_STATUS_UPDATED",
            "USER",
            "USR-100",
            "Updated account status.",
            "role=STAFF; status=PENDING_VERIFICATION",
            "role=STAFF; status=ACTIVE");

    assertThat(event.context().actorId()).isEqualTo("SYSTEM");
    assertThat(event.context().actorRole()).isEqualTo("SYSTEM");
    assertThat(event.beforeState()).contains("PENDING_VERIFICATION");
    assertThat(event.afterState()).contains("ACTIVE");
    assertThat(event.eventId()).isNotNull();
    assertThat(event.occurredAt()).isNotNull();
  }
}
