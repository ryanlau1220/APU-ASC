package com.apu.asc.config;

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

  EventPublicationMaintenance(
      IncompleteEventPublications incompletePublications,
      CompletedEventPublications completedPublications,
      @Value("${event-publications.retry.minimum-age:PT30S}") Duration retryMinimumAge,
      @Value("${event-publications.cleanup.retention:P30D}") Duration completionRetention) {
    this.incompletePublications = incompletePublications;
    this.completedPublications = completedPublications;
    this.retryMinimumAge = retryMinimumAge;
    this.completionRetention = completionRetention;
  }

  @Scheduled(
      fixedDelayString = "${event-publications.retry.fixed-delay-ms:60000}",
      initialDelayString = "${event-publications.retry.initial-delay-ms:60000}")
  void retryIncompletePublications() {
    incompletePublications.resubmitIncompletePublicationsOlderThan(retryMinimumAge);
  }

  @Scheduled(cron = "${event-publications.cleanup.cron:0 15 3 * * *}")
  void removeCompletedPublications() {
    completedPublications.deletePublicationsOlderThan(completionRetention);
    log.debug("Removed completed event publications older than {}", completionRetention);
  }
}
