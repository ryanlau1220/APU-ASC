package com.apu.asc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AuditLog {

  @NotBlank(message = "Log ID must not be blank")
  private String logId;

  @NotNull(message = "Timestamp must not be null")
  private LocalDateTime timestamp;

  @NotBlank(message = "User ID must not be blank")
  private String userId;

  @NotBlank(message = "Action type must not be blank")
  private String actionType;

  @NotBlank(message = "Target entity ID must not be blank")
  private String targetEntityId;

  @NotBlank(message = "Description must not be blank")
  private String description;

  public AuditLog(
      String logId,
      LocalDateTime timestamp,
      String userId,
      String actionType,
      String targetEntityId,
      String description) {
    this.logId = logId;
    this.timestamp = timestamp;
    this.userId = userId;
    this.actionType = actionType;
    this.targetEntityId = targetEntityId;
    this.description = description;
  }

  public String getLogId() {
    return logId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getUserId() {
    return userId;
  }

  public String getActionType() {
    return actionType;
  }

  public String getTargetEntityId() {
    return targetEntityId;
  }

  public String getDescription() {
    return description;
  }

  public String toFileString() {
    return String.join(
        "||", logId, timestamp.toString(), userId, actionType, targetEntityId, description);
  }
}
