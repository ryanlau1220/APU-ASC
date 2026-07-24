package com.apu.asc.servicecatalog;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceDto(
    String id,
    String categoryId,
    String name,
    String description,
    Integer durationMinutes,
    BigDecimal basePrice,
    String status,
    Instant createdAt) {}
