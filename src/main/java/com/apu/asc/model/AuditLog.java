package com.apu.asc.model;

import java.time.LocalDateTime;

public class AuditLog {

    private String logId;
    private LocalDateTime timestamp;
    private String userId;
    private String actionType;
    private String targetEntityId;
    private String description;

    public AuditLog(String logId, LocalDateTime timestamp, String userId,
                    String actionType, String targetEntityId, String description) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.actionType = actionType;
        this.targetEntityId = targetEntityId;
        this.description = description;
    }

    public String getLogId()             { return logId; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public String getUserId()            { return userId; }
    public String getActionType()        { return actionType; }
    public String getTargetEntityId()    { return targetEntityId; }
    public String getDescription()       { return description; }

    public String toFileString() {
        return String.join("||", logId, timestamp.toString(), userId,
                actionType, targetEntityId, description);
    }
}
