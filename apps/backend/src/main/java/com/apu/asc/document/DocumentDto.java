package com.apu.asc.document;

import java.time.Instant;

public record DocumentDto(
    String id,
    String workOrderId,
    DocumentType type,
    String fileName,
    String contentType,
    long sizeBytes,
    String uploadedBy,
    Instant createdAt) {}
