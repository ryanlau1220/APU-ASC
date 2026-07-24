package com.apu.asc.feedback;

import java.util.List;

public interface FeedbackApi {
  List<FeedbackDto> findAllFeedbacks();

  List<FeedbackDto> findByTechnician(String technicianId);

  FeedbackDto submitFeedback(FeedbackDto feedbackDto);
}
