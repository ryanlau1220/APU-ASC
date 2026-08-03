package com.apu.asc.config;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
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
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class SseController {

  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher eventPublisher;
  private final ConcurrentMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();
  private final ScheduledExecutorService heartbeatExecutor =
      Executors.newSingleThreadScheduledExecutor();

  public SseController(
      CurrentUserService currentUserService, ApplicationEventPublisher eventPublisher) {
    this.currentUserService = currentUserService;
    this.eventPublisher = eventPublisher;
    heartbeatExecutor.scheduleAtFixedRate(
        () -> {
          for (Map.Entry<String, Subscriber> entry : subscribers.entrySet()) {
            try {
              entry.getValue().emitter().send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
              removeSubscriber(entry.getKey(), entry.getValue());
            }
          }
        },
        15,
        15,
        TimeUnit.SECONDS);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "Subscribe to real-time event stream")
  public SseEmitter subscribe(Authentication authentication) {
    AuthenticatedUser user = currentUserService.requireCurrentUser(authentication);
    String subscriberId = UUID.randomUUID().toString();
    SseEmitter emitter = new SseEmitter(0L); // Infinite timeout for long-lived stream
    Subscriber subscriber = new Subscriber(user, emitter);

    emitter.onCompletion(() -> subscribers.remove(subscriberId, subscriber));
    emitter.onTimeout(() -> subscribers.remove(subscriberId, subscriber));
    emitter.onError((e) -> subscribers.remove(subscriberId, subscriber));
    subscribers.put(subscriberId, subscriber);

    try {
      emitter.send(
          SseEmitter.event()
              .name("connected")
              .data(Map.of("message", "Scoped live-update stream connected")));
    } catch (IOException e) {
      removeSubscriber(subscriberId, subscriber);
    }

    return emitter;
  }

  @Async
  @TransactionalEventListener(id = "live-update-delivery")
  public void handleLiveUpdateEvent(LiveUpdateEvent event) {
    int recipients = 0;
    for (Map.Entry<String, Subscriber> entry : subscribers.entrySet()) {
      if (event.isVisibleTo(entry.getValue().user())) {
        sendLiveUpdate(entry.getKey(), entry.getValue(), event);
        recipients++;
      }
    }
    log.debug(
        "Delivered scoped live update topic={} resourceId={} recipients={}",
        event.topic(),
        event.resourceId(),
        recipients);
  }

  private void sendLiveUpdate(String subscriberId, Subscriber subscriber, LiveUpdateEvent event) {
    try {
      subscriber
          .emitter()
          .send(
              SseEmitter.event()
                  .name("live-update")
                  .data(
                      new LiveUpdateMessage(
                          event.topic(), event.resourceId(), event.occurredAt())));
    } catch (Exception e) {
      removeSubscriber(subscriberId, subscriber);
    }
  }

  @PostMapping("/trigger/{entity}")
  @PreAuthorize("hasRole('MANAGER')")
  @Transactional
  @Operation(summary = "Trigger a manager-only live update (for testing)")
  public void triggerEvent(@PathVariable String entity) {
    eventPublisher.publishEvent(LiveUpdateEvent.forRoles(entity, null, "MANAGER"));
  }

  @PreDestroy
  void shutdownHeartbeat() {
    heartbeatExecutor.shutdownNow();
  }

  private void removeSubscriber(String subscriberId, Subscriber subscriber) {
    subscribers.remove(subscriberId, subscriber);
    try {
      subscriber.emitter().complete();
    } catch (Exception ignored) {
      // The client connection is already closed.
    }
  }

  private record Subscriber(AuthenticatedUser user, SseEmitter emitter) {}

  private record LiveUpdateMessage(String topic, String resourceId, java.time.Instant occurredAt) {}
}
