package com.apu.asc.quotation;

public enum QuotationStatus {
  DRAFT,
  PENDING_APPROVAL,
  APPROVED,
  REJECTED,
  EXPIRED;

  public static QuotationStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("A quotation status is required.");
    }
    try {
      return QuotationStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported quotation status: " + status);
    }
  }
}
