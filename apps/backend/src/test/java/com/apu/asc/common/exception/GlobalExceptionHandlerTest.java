package com.apu.asc.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {

  @Test
  void hidesUnexpectedExceptionDetailsFromClients() {
    var problem =
        new GlobalExceptionHandler().handleUnexpected(new RuntimeException("database detail"));

    assertThat(problem.getStatus()).isEqualTo(500);
    assertThat(problem.getDetail()).doesNotContain("database detail");
  }
}
