package com.apu.asc.feedback.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import com.apu.asc.user.CurrentUserService;
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
  private final AppointmentApi appointmentApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Get all customer feedback")
  public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
    return ResponseEntity.ok(feedbackApi.findAllFeedbacks());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get feedback entry by ID")
  public ResponseEntity<FeedbackDto> getFeedbackById(
      @PathVariable final String id, Authentication authentication) {
    FeedbackDto feedback = feedbackApi.getFeedbackById(id);
    accessPolicy.requireFeedbackRead(
        currentUserService.requireCurrentUser(authentication),
        feedback.customerId(),
        feedback.technicianId());
    return ResponseEntity.ok(feedback);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my submitted feedback")
  public ResponseEntity<List<FeedbackDto>> getMyFeedback(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    if (accessPolicy.isTechnician(currentUser)) {
      return ResponseEntity.ok(feedbackApi.findByTechnician(currentUser.id()));
    }
    return ResponseEntity.ok(feedbackApi.findByCustomer(currentUser.id()));
  }

  @GetMapping("/technician/{technicianId}")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get feedback by technician ID")
  public ResponseEntity<List<FeedbackDto>> getTechnicianFeedbacks(
      @PathVariable final String technicianId, Authentication authentication) {
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), technicianId);
    return ResponseEntity.ok(feedbackApi.findByTechnician(technicianId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'TECHNICIAN', 'MANAGER')")
  @Operation(summary = "Submit feedback or diagnostic report")
  public ResponseEntity<FeedbackDto> submitFeedback(
      @Valid @RequestBody final FeedbackDto feedbackDto, Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    AppointmentDto appointment = appointmentApi.getAppointmentById(feedbackDto.appointmentId());
    FeedbackDto securedFeedback = feedbackDto;

    if (!accessPolicy.isOperationalUser(currentUser)) {
      if (accessPolicy.isTechnician(currentUser)) {
        accessPolicy.requireAssignedTechnicianOrOperational(
            currentUser, appointment.technicianId());
        securedFeedback =
            new FeedbackDto(
                null,
                appointment.id(),
                appointment.customerId(),
                currentUser.id(),
                null,
                null,
                feedbackDto.technicianDiagnosticNotes(),
                null);
      } else {
        accessPolicy.requireSelfOrOperational(currentUser, appointment.customerId());
        securedFeedback =
            new FeedbackDto(
                null,
                appointment.id(),
                currentUser.id(),
                appointment.technicianId(),
                feedbackDto.rating(),
                feedbackDto.comments(),
                null,
                null);
      }
    }

    FeedbackDto created = feedbackApi.submitFeedback(securedFeedback);
    return ResponseEntity.created(URI.create("/api/v1/feedback/" + created.id())).body(created);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Delete feedback entry")
  public ResponseEntity<Void> deleteFeedback(@PathVariable final String id) {
    feedbackApi.deleteFeedback(id);
    return ResponseEntity.noContent().build();
  }
}
