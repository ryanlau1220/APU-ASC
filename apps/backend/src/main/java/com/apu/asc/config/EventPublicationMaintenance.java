package com.apu.asc.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Retries durable side effects and prunes only completed, aged event publications. */
@Component
@Slf4j
class EventPublicationMaintenance {

  private final IncompleteEventPublications incompletePublications;
  private final CompletedEventPublications completedPublications;
  private final Duration retryMinimumAge;
  private final Duration completionRetention;
  private final Counter retryRuns;
  private final Counter cleanupRuns;

  EventPublicationMaintenance(
      IncompleteEventPublications incompletePublications,
      CompletedEventPublications completedPublications,
      MeterRegistry meterRegistry,
      @Value("${event-publications.retry.minimum-age:PT30S}") Duration retryMinimumAge,
      @Value("${event-publications.cleanup.retention:P30D}") Duration completionRetention) {
    this.incompletePublications = incompletePublications;
    this.completedPublications = completedPublications;
    this.retryMinimumAge = retryMinimumAge;
    this.completionRetention = completionRetention;
    this.retryRuns = Counter.builder("event.publication.retry.runs").register(meterRegistry);
    this.cleanupRuns = Counter.builder("event.publication.cleanup.runs").register(meterRegistry);
  }

  @Scheduled(
      fixedDelayString = "${event-publications.retry.fixed-delay-ms:60000}",
      initialDelayString = "${event-publications.retry.initial-delay-ms:60000}")
  void retryIncompletePublications() {
    incompletePublications.resubmitIncompletePublicationsOlderThan(retryMinimumAge);
    retryRuns.increment();
  }

  @Scheduled(cron = "${event-publications.cleanup.cron:0 15 3 * * *}")
  void removeCompletedPublications() {
    completedPublications.deletePublicationsOlderThan(completionRetention);
    cleanupRuns.increment();
    log.debug("Removed completed event publications older than {}", completionRetention);
  }
}
