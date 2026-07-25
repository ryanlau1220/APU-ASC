package com.apu.asc.vehicle;

import java.util.List;

public interface VehicleApi {
  List<VehicleDto> findAllVehicles();

  List<VehicleDto> findVehiclesByCustomer(String customerId);

  VehicleDto createVehicle(VehicleDto vehicleDto);
}
