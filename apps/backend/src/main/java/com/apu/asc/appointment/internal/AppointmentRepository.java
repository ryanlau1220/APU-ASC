package com.apu.asc.appointment.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface AppointmentRepository extends JpaRepository<AppointmentEntity, String> {
  List<AppointmentEntity> findByCustomerId(String customerId);

  List<AppointmentEntity> findByTechnicianId(String technicianId);

  boolean existsByTechnicianIdAndAppointmentDateAndTimeSlotAndStatusNot(
      String technicianId, LocalDate appointmentDate, String timeSlot, String status);

  boolean existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
      String customerId, LocalDate appointmentDate, String timeSlot, String status);

  List<AppointmentEntity> findByAppointmentDateAndStatusAndReminderQueuedAtIsNull(
      LocalDate appointmentDate, String status);

  @Modifying(flushAutomatically = true)
  @Query(
      "update AppointmentEntity appointment set appointment.reminderQueuedAt = :queuedAt "
          + "where appointment.id = :id and appointment.status = :status "
          + "and appointment.reminderQueuedAt is null")
  int claimReminder(
      @Param("id") String id, @Param("status") String status, @Param("queuedAt") Instant queuedAt);
}
