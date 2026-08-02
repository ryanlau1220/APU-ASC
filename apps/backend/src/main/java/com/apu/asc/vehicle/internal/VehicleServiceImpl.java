package com.apu.asc.vehicle.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class VehicleServiceImpl implements VehicleApi {

  private final VehicleRepository vehicleRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<VehicleDto> findAllVehicles() {
    return vehicleRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VehicleDto getVehicleById(final String id) {
    return vehicleRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<VehicleDto> findVehiclesByCustomer(final String customerId) {
    return vehicleRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public VehicleDto createVehicle(final VehicleDto vehicleDto) {
    String id = vehicleDto.id() != null ? vehicleDto.id() : "VEH-" + UUID.randomUUID().toString();
    VehicleEntity entity =
        VehicleEntity.builder()
            .id(id)
            .customerId(vehicleDto.customerId())
            .licensePlate(vehicleDto.licensePlate())
            .make(vehicleDto.make())
            .model(vehicleDto.model())
            .yearOfManufacture(vehicleDto.yearOfManufacture())
            .build();
    VehicleDto created = toDto(vehicleRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            created.customerId(),
            "VEHICLE_REGISTERED",
            "VEHICLE",
            created.id(),
            "Registered vehicle "
                + created.licensePlate()
                + " ("
                + created.make()
                + " "
                + created.model()
                + ")"));
    return created;
  }

  @Override
  @Transactional
  public VehicleDto updateVehicle(final String id, final VehicleDto vehicleDto) {
    VehicleEntity entity =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

    if (vehicleDto.licensePlate() != null) entity.setLicensePlate(vehicleDto.licensePlate());
    if (vehicleDto.make() != null) entity.setMake(vehicleDto.make());
    if (vehicleDto.model() != null) entity.setModel(vehicleDto.model());
    if (vehicleDto.yearOfManufacture() != 0)
      entity.setYearOfManufacture(vehicleDto.yearOfManufacture());

    VehicleDto updated = toDto(vehicleRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "VEHICLE_UPDATED",
            "VEHICLE",
            updated.id(),
            "Updated vehicle details for " + updated.licensePlate()));
    return updated;
  }

  @Override
  @Transactional
  public void deleteVehicle(final String id) {
    VehicleEntity entity =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    vehicleRepository.deleteById(id);
    eventPublisher.publishEvent(
        new AuditEvent(
            entity.getCustomerId(),
            "VEHICLE_DELETED",
            "VEHICLE",
            id,
            "Deregistered/deleted vehicle " + entity.getLicensePlate()));
  }

  private VehicleDto toDto(VehicleEntity entity) {
    return new VehicleDto(
        entity.getId(),
        entity.getCustomerId(),
        entity.getLicensePlate(),
        entity.getMake(),
        entity.getModel(),
        entity.getYearOfManufacture(),
        entity.getCreatedAt());
  }
}
