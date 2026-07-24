package com.apu.asc.vehicle.internal;

import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class VehicleServiceImpl implements VehicleApi {

  private final VehicleRepository vehicleRepository;

  @Override
  @Transactional(readOnly = true)
  public List<VehicleDto> findVehiclesByCustomer(final String customerId) {
    return vehicleRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public VehicleDto createVehicle(final VehicleDto vehicleDto) {
    String id =
        vehicleDto.id() != null
            ? vehicleDto.id()
            : "VEH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    VehicleEntity entity =
        VehicleEntity.builder()
            .id(id)
            .customerId(vehicleDto.customerId())
            .licensePlate(vehicleDto.licensePlate())
            .make(vehicleDto.make())
            .model(vehicleDto.model())
            .yearOfManufacture(vehicleDto.yearOfManufacture())
            .build();
    return toDto(vehicleRepository.save(entity));
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
