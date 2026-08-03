package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.appointment.AppointmentStatus;
import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.SseBroadcastEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AppointmentServiceImpl implements AppointmentApi {

  private final AppointmentRepository appointmentRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentDto> findAllAppointments() {
    return appointmentRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentDto getAppointmentById(final String id) {
    return appointmentRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentDto> findByCustomer(final String customerId) {
    return appointmentRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentDto> findByTechnician(final String technicianId) {
    return appointmentRepository.findByTechnicianId(technicianId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public AppointmentDto createAppointment(final AppointmentDto appointmentDto) {
    validateRequiredBookingFields(appointmentDto);
    AppointmentStatus status =
        appointmentDto.status() == null || appointmentDto.status().isBlank()
            ? AppointmentStatus.PENDING
            : AppointmentStatus.fromString(appointmentDto.status());
    if (status != AppointmentStatus.PENDING) {
      throw new IllegalArgumentException("New appointments must start in PENDING status.");
    }

    // C7: Double-booking prevention
    if (appointmentDto.customerId() != null
        && appointmentDto.appointmentDate() != null
        && appointmentDto.timeSlot() != null
        && appointmentRepository.existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
            appointmentDto.customerId(),
            appointmentDto.appointmentDate(),
            appointmentDto.timeSlot(),
            "CANCELLED")) {
      throw new IllegalArgumentException(
          "Customer already has an appointment booked for this date and time slot.");
    }

    if (appointmentDto.technicianId() != null
        && !appointmentDto.technicianId().isBlank()
        && appointmentDto.appointmentDate() != null
        && appointmentDto.timeSlot() != null
        && appointmentRepository.existsByTechnicianIdAndAppointmentDateAndTimeSlotAndStatusNot(
            appointmentDto.technicianId(),
            appointmentDto.appointmentDate(),
            appointmentDto.timeSlot(),
            "CANCELLED")) {
      throw new IllegalArgumentException(
          "Technician already has an active appointment assigned for this date and time slot.");
    }

    String id =
        appointmentDto.id() != null ? appointmentDto.id() : "APT-" + UUID.randomUUID().toString();
    AppointmentEntity entity =
        AppointmentEntity.builder()
            .id(id)
            .customerId(appointmentDto.customerId())
            .vehicleId(appointmentDto.vehicleId())
            .serviceId(appointmentDto.serviceId())
            .technicianId(appointmentDto.technicianId())
            .appointmentDate(appointmentDto.appointmentDate())
            .timeSlot(appointmentDto.timeSlot())
            .status(status.name())
            .notes(appointmentDto.notes())
            .build();

    // Flush before publishing events so a rejected database write cannot produce a false audit
    // entry or a live-update notification.
    AppointmentDto created = toDto(appointmentRepository.saveAndFlush(entity));
    eventPublisher.publishEvent(new SseBroadcastEvent("appointments"));
    eventPublisher.publishEvent(
        new AuditEvent(
            created.customerId(),
            "APPOINTMENT_CREATED",
            "APPOINTMENT",
            created.id(),
            "Booked appointment on " + created.appointmentDate()));
    return created;
  }

  @Override
  @Transactional
  public AppointmentDto updateAppointment(final String id, final AppointmentDto appointmentDto) {
    AppointmentEntity entity =
        appointmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

    if (appointmentDto.appointmentDate() != null)
      entity.setAppointmentDate(appointmentDto.appointmentDate());
    if (appointmentDto.timeSlot() != null) entity.setTimeSlot(appointmentDto.timeSlot());
    if (appointmentDto.notes() != null) entity.setNotes(appointmentDto.notes());
    if (appointmentDto.technicianId() != null)
      entity.setTechnicianId(appointmentDto.technicianId());
    if (appointmentDto.status() != null) {
      transition(entity, AppointmentStatus.fromString(appointmentDto.status()));
    }

    AppointmentDto updated = toDto(appointmentRepository.save(entity));
    eventPublisher.publishEvent(new SseBroadcastEvent("appointments"));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "APPOINTMENT_UPDATED",
            "APPOINTMENT",
            updated.id(),
            "Updated appointment details"));
    return updated;
  }

  @Override
  @Transactional
  public AppointmentDto updateStatus(final String id, final String status) {
    AppointmentEntity entity =
        appointmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

    transition(entity, AppointmentStatus.fromString(status));
    AppointmentDto updated = toDto(appointmentRepository.save(entity));
    eventPublisher.publishEvent(new SseBroadcastEvent("appointments"));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "APPOINTMENT_STATUS_UPDATED",
            "APPOINTMENT",
            updated.id(),
            "Status changed to " + status));
    return updated;
  }

  @Override
  @Transactional
  public void deleteAppointment(final String id) {
    AppointmentEntity entity =
        appointmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

    appointmentRepository.delete(entity);
    eventPublisher.publishEvent(new SseBroadcastEvent("appointments"));
    eventPublisher.publishEvent(
        new AuditEvent(
            entity.getCustomerId(),
            "APPOINTMENT_DELETED",
            "APPOINTMENT",
            id,
            "Cancelled/deleted appointment"));
  }

  private AppointmentDto toDto(AppointmentEntity entity) {
    return new AppointmentDto(
        entity.getId(),
        entity.getCustomerId(),
        entity.getVehicleId(),
        entity.getServiceId(),
        entity.getTechnicianId(),
        entity.getAppointmentDate(),
        entity.getTimeSlot(),
        entity.getStatus(),
        entity.getNotes(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private void validateRequiredBookingFields(AppointmentDto appointmentDto) {
    if (appointmentDto.customerId() == null || appointmentDto.customerId().isBlank()) {
      throw new IllegalArgumentException("A customer is required to book an appointment.");
    }
    if (appointmentDto.vehicleId() == null || appointmentDto.vehicleId().isBlank()) {
      throw new IllegalArgumentException("A vehicle is required to book an appointment.");
    }
    if (appointmentDto.serviceId() == null || appointmentDto.serviceId().isBlank()) {
      throw new IllegalArgumentException("A service is required to book an appointment.");
    }
    if (appointmentDto.appointmentDate() == null) {
      throw new IllegalArgumentException("An appointment date is required to book an appointment.");
    }
    if (appointmentDto.timeSlot() == null || appointmentDto.timeSlot().isBlank()) {
      throw new IllegalArgumentException("A time slot is required to book an appointment.");
    }
  }

  private void transition(AppointmentEntity entity, AppointmentStatus target) {
    AppointmentStatus current = AppointmentStatus.fromString(entity.getStatus());
    current.requireTransitionTo(target);
    entity.setStatus(target.name());
  }
}
