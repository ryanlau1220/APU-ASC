package com.apu.asc.servicecatalog;

import java.time.Instant;

public record CategoryDto(String id, String name, String description, Instant createdAt) {}
