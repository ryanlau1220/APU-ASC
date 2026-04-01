package com.apu.asc.model;

import com.apu.asc.util.validation.Max;
import com.apu.asc.util.validation.Min;
import com.apu.asc.util.validation.NotBlank;
import com.apu.asc.util.validation.Size;

public class Feedback {

  private final String feedbackId;
  private final String appointmentId;

  @Min(value = 1, message = "err.validation.ratingMin|{value}")
  @Max(value = 5, message = "err.validation.ratingMax|{value}")
  private final int rating;

  @NotBlank(message = "err.validation.commentsRequired")
  @Size(max = 500, message = "err.validation.commentsMax|{max}")
  private final String comments;

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
