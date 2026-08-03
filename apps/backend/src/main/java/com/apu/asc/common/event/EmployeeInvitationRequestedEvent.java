package com.apu.asc.common.event;

/**
 * Requests an employee invitation email after the employee record has committed.
 *
 * <p>The raw invitation token is intentionally generated only by the listener and is never
 * persisted in the event publication table.
 */
public record EmployeeInvitationRequestedEvent(String userId) {

  public EmployeeInvitationRequestedEvent {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("An employee user ID is required.");
    }
    userId = userId.trim();
  }
}
