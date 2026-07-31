package com.apu.asc.feedback.internal;

import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FeedbackServiceImpl implements FeedbackApi {

  private final FeedbackRepository feedbackRepository;

  @Override
  @Transactional(readOnly = true)
  public List<FeedbackDto> findAllFeedbacks() {
    return feedbackRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public FeedbackDto getFeedbackById(final String id) {
    return feedbackRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<FeedbackDto> findByCustomer(final String customerId) {
    return feedbackRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FeedbackDto> findByTechnician(final String technicianId) {
    return feedbackRepository.findByTechnicianId(technicianId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public FeedbackDto submitFeedback(final FeedbackDto feedbackDto) {
    String id =
        feedbackDto.id() != null
            ? feedbackDto.id()
            : "FBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    FeedbackEntity entity =
        FeedbackEntity.builder()
            .id(id)
            .appointmentId(feedbackDto.appointmentId())
            .customerId(feedbackDto.customerId())
            .technicianId(feedbackDto.technicianId())
            .rating(feedbackDto.rating())
            .comments(feedbackDto.comments())
            .technicianDiagnosticNotes(feedbackDto.technicianDiagnosticNotes())
            .build();
    return toDto(feedbackRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteFeedback(final String id) {
    if (!feedbackRepository.existsById(id)) {
      throw new ResourceNotFoundException("Feedback", id);
    }
    feedbackRepository.deleteById(id);
  }

  private FeedbackDto toDto(FeedbackEntity entity) {
    return new FeedbackDto(
        entity.getId(),
        entity.getAppointmentId(),
        entity.getCustomerId(),
        entity.getTechnicianId(),
        entity.getRating(),
        entity.getComments(),
        entity.getTechnicianDiagnosticNotes(),
        entity.getCreatedAt());
  }
}
