package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseControllerTest {

  @Test
  @DisplayName("Should create long-lived SseEmitter for stream subscribers")
  void shouldSubscribeToSseStream() {
    SseController controller = new SseController();
    SseEmitter emitter = controller.subscribe();

    assertThat(emitter).isNotNull();
  }

  @Test
  @DisplayName("Should broadcast entity invalidate event without throwing exceptions")
  void shouldPublishInvalidateEvent() {
    SseController controller = new SseController();
    controller.subscribe();

    controller.publishInvalidateEvent("appointments");
    controller.publishInvalidateEvent("services");
  }
}
