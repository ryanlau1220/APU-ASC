package com.apu.asc.feedback.internal;

import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback & Reviews", description = "Diagnostic feedback and customer review APIs")
class FeedbackController {

  private final FeedbackApi feedbackApi;

  @GetMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Get all customer feedback")
  public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
    return ResponseEntity.ok(feedbackApi.findAllFeedbacks());
  }

  @GetMapping("/technician/{technicianId}")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER')")
  @Operation(summary = "Get feedback by technician ID")
  public ResponseEntity<List<FeedbackDto>> getTechnicianFeedbacks(
      @PathVariable final String technicianId) {
    return ResponseEntity.ok(feedbackApi.findByTechnician(technicianId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
  @Operation(summary = "Submit feedback or diagnostic report")
  public ResponseEntity<FeedbackDto> submitFeedback(@RequestBody final FeedbackDto feedbackDto) {
    return ResponseEntity.ok(feedbackApi.submitFeedback(feedbackDto));
  }
}
