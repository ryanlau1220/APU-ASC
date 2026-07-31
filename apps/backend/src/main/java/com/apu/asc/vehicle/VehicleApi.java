package com.apu.asc.vehicle;

import java.util.List;

public interface VehicleApi {
  List<VehicleDto> findAllVehicles();

  VehicleDto getVehicleById(String id);

  List<VehicleDto> findVehiclesByCustomer(String customerId);

  VehicleDto createVehicle(VehicleDto vehicleDto);

  VehicleDto updateVehicle(String id, VehicleDto vehicleDto);

  void deleteVehicle(String id);
}
