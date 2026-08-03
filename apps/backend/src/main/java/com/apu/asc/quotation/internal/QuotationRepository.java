package com.apu.asc.quotation.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface QuotationRepository extends JpaRepository<QuotationEntity, String> {
  List<QuotationEntity> findAllByOrderByCreatedAtDesc();

  List<QuotationEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);

  List<QuotationEntity> findByWorkOrderIdOrderByRevisionDesc(String workOrderId);

  Optional<QuotationEntity> findFirstByWorkOrderIdAndStatusOrderByRevisionDesc(
      String workOrderId, String status);

  int countByWorkOrderId(String workOrderId);
}
