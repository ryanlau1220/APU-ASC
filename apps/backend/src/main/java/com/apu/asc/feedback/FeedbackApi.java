package com.apu.asc.feedback;

import java.util.List;

public interface FeedbackApi {
  List<FeedbackDto> findAllFeedbacks();

  FeedbackDto getFeedbackById(String id);

  List<FeedbackDto> findByCustomer(String customerId);

  List<FeedbackDto> findByTechnician(String technicianId);

  FeedbackDto submitFeedback(FeedbackDto feedbackDto);

  void deleteFeedback(String id);
}
