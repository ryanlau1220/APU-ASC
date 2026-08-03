package com.apu.asc.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmployeeInvitationRequestedEventTest {

  @Test
  void retainsOnlyTheEmployeeIdentifier() {
    EmployeeInvitationRequestedEvent event = new EmployeeInvitationRequestedEvent(" USR-100 ");

    assertThat(event.userId()).isEqualTo("USR-100");
  }

  @Test
  void rejectsBlankEmployeeIdentifier() {
    assertThatThrownBy(() -> new EmployeeInvitationRequestedEvent(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("user ID");
  }
}
