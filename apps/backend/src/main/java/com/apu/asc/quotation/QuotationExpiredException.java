package com.apu.asc.quotation;

/** Signals that an approval was refused because the submitted estimate is no longer valid. */
public class QuotationExpiredException extends IllegalArgumentException {

  public QuotationExpiredException() {
    super("This quotation has expired and can no longer be approved.");
  }
}
