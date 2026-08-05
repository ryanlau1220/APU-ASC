package com.apu.asc.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPreferencesUpdateRequest(
    @NotBlank @Size(max = 64) String timeZone, boolean inAppNotificationsEnabled) {}
