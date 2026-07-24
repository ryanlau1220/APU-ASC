package com.apu.asc.user;

import java.time.Instant;

public record UserDto(
    String id,
    String keycloakId,
    String username,
    String email,
    String fullName,
    String contactNumber,
    String role,
    String status,
    Instant createdAt,
    Instant updatedAt) {}
