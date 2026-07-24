package com.apu.asc.appointment.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentRepository extends JpaRepository<AppointmentEntity, String> {
  List<AppointmentEntity> findByCustomerId(String customerId);

  List<AppointmentEntity> findByTechnicianId(String technicianId);
}
