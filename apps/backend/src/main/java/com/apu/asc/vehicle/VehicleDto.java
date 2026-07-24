package com.apu.asc.vehicle;

import java.time.Instant;

public record VehicleDto(
    String id,
    String customerId,
    String licensePlate,
    String make,
    String model,
    Integer yearOfManufacture,
    Instant createdAt) {}
