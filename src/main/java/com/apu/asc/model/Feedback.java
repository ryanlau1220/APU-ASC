package com.apu.asc.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Feedback {

  private String feedbackId;
  private String appointmentId;

  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private int rating;

  @NotBlank(message = "Comments cannot be empty")
  @Size(max = 500, message = "Comments cannot exceed 500 characters")
  private String comments;

  public Feedback(String feedbackId, String appointmentId, int rating, String comments) {
    this.feedbackId = feedbackId;
    this.appointmentId = appointmentId;
    this.rating = rating;
    this.comments = comments;
  }

  public String getFeedbackId() {
    return feedbackId;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public int getRating() {
    return rating;
  }

  public String getComments() {
    return comments;
  }

  public String toFileString() {
    return String.join("||", feedbackId, appointmentId, String.valueOf(rating), comments);
  }
}
