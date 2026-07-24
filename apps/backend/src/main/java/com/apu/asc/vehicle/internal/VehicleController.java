package com.apu.asc.vehicle.internal;

import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Customer vehicle registration and management APIs")
class VehicleController {

  private final VehicleApi vehicleApi;

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get vehicles by customer ID")
  public ResponseEntity<List<VehicleDto>> getVehiclesByCustomer(
      @PathVariable final String customerId) {
    return ResponseEntity.ok(vehicleApi.findVehiclesByCustomer(customerId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Register vehicle")
  public ResponseEntity<VehicleDto> createVehicle(@RequestBody final VehicleDto vehicleDto) {
    return ResponseEntity.ok(vehicleApi.createVehicle(vehicleDto));
  }
}
