package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AppointmentServiceImpl implements AppointmentApi {

  private final AppointmentRepository appointmentRepository;

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
    String id =
        appointmentDto.id() != null
            ? appointmentDto.id()
            : "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    AppointmentEntity entity =
        AppointmentEntity.builder()
            .id(id)
            .customerId(appointmentDto.customerId())
            .vehicleId(appointmentDto.vehicleId())
            .serviceId(appointmentDto.serviceId())
            .technicianId(appointmentDto.technicianId())
            .appointmentDate(appointmentDto.appointmentDate())
            .timeSlot(appointmentDto.timeSlot())
            .status(appointmentDto.status() != null ? appointmentDto.status() : "PENDING")
            .notes(appointmentDto.notes())
            .build();
    return toDto(appointmentRepository.save(entity));
  }

  @Override
  @Transactional
  public AppointmentDto updateAppointment(final String id, final AppointmentDto appointmentDto) {
    AppointmentEntity entity =
        appointmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

    if (appointmentDto.vehicleId() != null) entity.setVehicleId(appointmentDto.vehicleId());
    if (appointmentDto.serviceId() != null) entity.setServiceId(appointmentDto.serviceId());
    if (appointmentDto.technicianId() != null)
      entity.setTechnicianId(appointmentDto.technicianId());
    if (appointmentDto.appointmentDate() != null)
      entity.setAppointmentDate(appointmentDto.appointmentDate());
    if (appointmentDto.timeSlot() != null) entity.setTimeSlot(appointmentDto.timeSlot());
    if (appointmentDto.status() != null) entity.setStatus(appointmentDto.status());
    if (appointmentDto.notes() != null) entity.setNotes(appointmentDto.notes());

    return toDto(appointmentRepository.save(entity));
  }

  @Override
  @Transactional
  public AppointmentDto updateStatus(final String id, final String status) {
    AppointmentEntity entity =
        appointmentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    entity.setStatus(status);
    return toDto(appointmentRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteAppointment(final String id) {
    if (!appointmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Appointment", id);
    }
    appointmentRepository.deleteById(id);
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
}
