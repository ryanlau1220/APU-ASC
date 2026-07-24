package com.apu.asc.feedback.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface FeedbackRepository extends JpaRepository<FeedbackEntity, String> {
  List<FeedbackEntity> findByCustomerId(String customerId);

  List<FeedbackEntity> findByTechnicianId(String technicianId);
}
