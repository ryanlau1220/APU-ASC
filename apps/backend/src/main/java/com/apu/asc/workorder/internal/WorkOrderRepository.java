package com.apu.asc.workorder.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, String> {
  Optional<WorkOrderEntity> findByAppointmentId(String appointmentId);

  List<WorkOrderEntity> findByCustomerId(String customerId);

  List<WorkOrderEntity> findByTechnicianId(String technicianId);
}
