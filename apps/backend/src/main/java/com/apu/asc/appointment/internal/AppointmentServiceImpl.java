package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
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
