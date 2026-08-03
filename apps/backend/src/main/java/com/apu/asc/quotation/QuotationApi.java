package com.apu.asc.quotation;

import java.util.List;

public interface QuotationApi {
  List<QuotationDto> findAllQuotations();

  List<QuotationDto> findByCustomer(String customerId);

  List<QuotationDto> findByWorkOrder(String workOrderId);

  QuotationDto getQuotationById(String id);

  QuotationDto createDraft(String customerId, QuotationDraftRequestDto request);

  QuotationDto updateDraft(String id, QuotationDraftRequestDto request);

  QuotationDto submit(String id);

  QuotationDto decide(String id, QuotationDecisionRequestDto request);

  QuotationDto getApprovedByWorkOrder(String workOrderId);
}
