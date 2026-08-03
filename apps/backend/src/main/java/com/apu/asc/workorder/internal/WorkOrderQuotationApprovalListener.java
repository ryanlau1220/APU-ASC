package com.apu.asc.workorder.internal;

import com.apu.asc.common.event.QuotationApprovedEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Records the accepted estimate locally so execution can enforce customer approval. */
@Component
@RequiredArgsConstructor
class WorkOrderQuotationApprovalListener {

  private final WorkOrderRepository workOrderRepository;

  @EventListener
  @Transactional
  void on(QuotationApprovedEvent event) {
    WorkOrderEntity workOrder =
        workOrderRepository
            .findById(event.workOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Work order", event.workOrderId()));
    workOrder.setApprovedQuotationId(event.quotationId());
    workOrder.setQuotationApprovedAt(event.approvedAt());
    workOrderRepository.save(workOrder);
  }
}
