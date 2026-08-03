package com.apu.asc.feedback.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.feedback.FeedbackDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

  @Mock private FeedbackRepository feedbackRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private FeedbackServiceImpl feedbackService;

  @BeforeEach
  void setUp() {
    feedbackService = new FeedbackServiceImpl(feedbackRepository);
  }

  @Test
  @DisplayName("Should submit customer feedback")
  void shouldSubmitFeedback() {
    FeedbackEntity entity =
        FeedbackEntity.builder()
            .id("FBK-100")
            .appointmentId("APT-1")
            .customerId("CUST-1")
            .rating(5)
            .comments("Excellent service!")
            .createdAt(Instant.now())
            .build();

    when(feedbackRepository.save(any())).thenReturn(entity);

    FeedbackDto dto =
        new FeedbackDto(null, "APT-1", "CUST-1", null, 5, "Excellent service!", null, null, "WO-1");

    FeedbackDto created = feedbackService.submitFeedback(dto);

    assertThat(created).isNotNull();
    assertThat(created.rating()).isEqualTo(5);
    assertThat(created.comments()).isEqualTo("Excellent service!");
    verify(feedbackRepository).save(any());
  }

  @Test
  @DisplayName("Should find feedback by customer ID")
  void shouldFindByCustomer() {
    FeedbackEntity entity =
        FeedbackEntity.builder()
            .id("FBK-100")
            .appointmentId("APT-1")
            .customerId("CUST-1")
            .rating(4)
            .comments("Good job")
            .createdAt(Instant.now())
            .build();

    when(feedbackRepository.findByCustomerId("CUST-1")).thenReturn(List.of(entity));

    List<FeedbackDto> results = feedbackService.findByCustomer("CUST-1");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).rating()).isEqualTo(4);
  }
}
