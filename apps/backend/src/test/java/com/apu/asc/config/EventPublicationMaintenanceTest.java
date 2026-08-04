package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.IncompleteEventPublications;

class EventPublicationMaintenanceTest {

  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

  @AfterEach
  void closeMeterRegistry() {
    meterRegistry.close();
  }

  @Test
  @DisplayName("Records retry and cleanup maintenance runs")
  void recordsMaintenanceRuns() {
    Duration retryMinimumAge = Duration.ofSeconds(30);
    Duration completionRetention = Duration.ofDays(30);
    AtomicReference<Duration> retriedAfter = new AtomicReference<>();
    AtomicReference<Duration> cleanedAfter = new AtomicReference<>();
    EventPublicationMaintenance maintenance =
        new EventPublicationMaintenance(
            incompletePublications(retriedAfter),
            completedPublications(cleanedAfter),
            meterRegistry,
            retryMinimumAge,
            completionRetention);

    maintenance.retryIncompletePublications();
    maintenance.removeCompletedPublications();

    assertThat(retriedAfter).hasValue(retryMinimumAge);
    assertThat(cleanedAfter).hasValue(completionRetention);
    assertThat(meterRegistry.counter("event.publication.retry.runs").count()).isEqualTo(1.0);
    assertThat(meterRegistry.counter("event.publication.cleanup.runs").count()).isEqualTo(1.0);
  }

  private IncompleteEventPublications incompletePublications(
      AtomicReference<Duration> retriedAfter) {
    return new IncompleteEventPublications() {
      @Override
      public void resubmitIncompletePublications(Predicate<EventPublication> filter) {
        // Not used by the scheduled retry path.
      }

      @Override
      public void resubmitIncompletePublicationsOlderThan(Duration duration) {
        retriedAfter.set(duration);
      }
    };
  }

  private CompletedEventPublications completedPublications(AtomicReference<Duration> cleanedAfter) {
    return new CompletedEventPublications() {
      @Override
      public Collection<? extends EventPublication> findAll() {
        return List.of();
      }

      @Override
      public void deletePublications(Predicate<EventPublication> filter) {
        // Not used by the scheduled cleanup path.
      }

      @Override
      public void deletePublicationsOlderThan(Duration duration) {
        cleanedAfter.set(duration);
      }
    };
  }
}
