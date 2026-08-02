package com.apu.asc.feedback.internal;

import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback & Reviews", description = "Diagnostic feedback and customer review APIs")
class FeedbackController {

  private final FeedbackApi feedbackApi;
  private final UserApi userApi;

  @GetMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Get all customer feedback")
  public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
    return ResponseEntity.ok(feedbackApi.findAllFeedbacks());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get feedback entry by ID")
  public ResponseEntity<FeedbackDto> getFeedbackById(@PathVariable final String id) {
    return ResponseEntity.ok(feedbackApi.getFeedbackById(id));
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my submitted feedback")
  public ResponseEntity<List<FeedbackDto>> getMyFeedback(Authentication authentication) {
    String currentUserId = resolveUserId(authentication);
    if (currentUserId == null) {
      return ResponseEntity.ok(List.of());
    }
    return ResponseEntity.ok(feedbackApi.findByCustomer(currentUserId));
  }

  @GetMapping("/technician/{technicianId}")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER')")
  @Operation(summary = "Get feedback by technician ID")
  public ResponseEntity<List<FeedbackDto>> getTechnicianFeedbacks(
      @PathVariable final String technicianId) {
    return ResponseEntity.ok(feedbackApi.findByTechnician(technicianId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'TECHNICIAN', 'MANAGER')")
  @Operation(summary = "Submit feedback or diagnostic report")
  public ResponseEntity<FeedbackDto> submitFeedback(
      @Valid @RequestBody final FeedbackDto feedbackDto) {
    FeedbackDto created = feedbackApi.submitFeedback(feedbackDto);
    return ResponseEntity.created(URI.create("/api/v1/feedback/" + created.id())).body(created);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Delete feedback entry")
  public ResponseEntity<Void> deleteFeedback(@PathVariable final String id) {
    feedbackApi.deleteFeedback(id);
    return ResponseEntity.noContent().build();
  }

  private String resolveUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    String name = authentication.getName();
    return userApi
        .findByUsername(name)
        .or(() -> userApi.findByEmail(name))
        .or(() -> userApi.findByKeycloakId(name))
        .map(UserDto::id)
        .orElse(name);
  }
}
