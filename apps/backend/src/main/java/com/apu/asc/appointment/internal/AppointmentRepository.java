package com.apu.asc.appointment.internal;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentRepository extends JpaRepository<AppointmentEntity, String> {
  List<AppointmentEntity> findByCustomerId(String customerId);

  List<AppointmentEntity> findByTechnicianId(String technicianId);

  boolean existsByTechnicianIdAndAppointmentDateAndTimeSlotAndStatusNot(
      String technicianId, LocalDate appointmentDate, String timeSlot, String status);

  boolean existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
      String customerId, LocalDate appointmentDate, String timeSlot, String status);
}
