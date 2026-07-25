package com.apu.asc.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Stream", description = "Real-time Server-Sent Events (SSE) streaming API")
@Slf4j
public class SseController {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  private final ScheduledExecutorService heartbeatExecutor =
      Executors.newSingleThreadScheduledExecutor();

  public SseController() {
    // Periodically send ping comments to keep connections alive through reverse proxies
    heartbeatExecutor.scheduleAtFixedRate(
        () -> {
          for (SseEmitter emitter : emitters) {
            try {
              emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
              emitters.remove(emitter);
            }
          }
        },
        15,
        15,
        TimeUnit.SECONDS);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "Subscribe to real-time event stream")
  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(0L); // Infinite timeout for long-lived stream

    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((e) -> emitters.remove(emitter));

    try {
      emitter.send(
          SseEmitter.event().name("connected").data(Map.of("message", "SSE stream connected")));
    } catch (IOException e) {
      emitters.remove(emitter);
    }

    return emitter;
  }

  public void publishInvalidateEvent(String entityName) {
    log.info("Broadcasting SSE invalidate event for entity: {}", entityName);
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("invalidate").data(Map.of("entity", entityName)));
      } catch (Exception e) {
        emitters.remove(emitter);
      }
    }
  }

  @PostMapping("/trigger/{entity}")
  @Operation(summary = "Trigger SSE invalidate event (for testing)")
  public void triggerEvent(@PathVariable String entity) {
    publishInvalidateEvent(entity);
  }
}
