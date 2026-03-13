package com.apu.asc.model;

public class Feedback {

    private String feedbackId;
    private String appointmentId;
    private int rating;
    private String comments;

    public Feedback(String feedbackId, String appointmentId, int rating, String comments) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comments = comments;
    }

    public String getFeedbackId()   { return feedbackId; }
    public String getAppointmentId() { return appointmentId; }
    public int getRating()          { return rating; }
    public String getComments()     { return comments; }

    public String toFileString() {
        return String.join("||", feedbackId, appointmentId,
                String.valueOf(rating), comments);
    }
}
