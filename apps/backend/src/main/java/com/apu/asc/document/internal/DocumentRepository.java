package com.apu.asc.document.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
  List<DocumentEntity> findByWorkOrderIdOrderByCreatedAtDesc(String workOrderId);
}
