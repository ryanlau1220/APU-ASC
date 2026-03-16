package com.apu.asc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AuditLog {

  @NotBlank(message = "err.validation.logIdRequired")
  private final String logId;

  @NotNull(message = "err.validation.timestampRequired")
  private final LocalDateTime timestamp;

  @NotBlank(message = "err.validation.userIdRequired")
  private final String userId;

  @NotBlank(message = "err.validation.actionTypeRequired")
  private final String actionType;

  @NotBlank(message = "err.validation.targetEntityRequired")
  private final String targetEntityId;

  @NotBlank(message = "err.validation.descriptionRequired")
  private final String description;

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
