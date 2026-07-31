package com.apu.asc.vehicle.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Customer vehicle registration and management APIs")
class VehicleController {

  private final VehicleApi vehicleApi;
  private final UserApi userApi;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all registered vehicles")
  public ResponseEntity<List<VehicleDto>> getAllVehicles() {
    return ResponseEntity.ok(vehicleApi.findAllVehicles());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get vehicle by ID")
  public ResponseEntity<VehicleDto> getVehicleById(@PathVariable final String id) {
    return ResponseEntity.ok(vehicleApi.getVehicleById(id));
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my registered vehicles")
  public ResponseEntity<List<VehicleDto>> getMyVehicles(Authentication authentication) {
    String currentUserId = resolveUserId(authentication);
    if (currentUserId == null) {
      return ResponseEntity.ok(List.of());
    }
    return ResponseEntity.ok(vehicleApi.findVehiclesByCustomer(currentUserId));
  }

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
  public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody final VehicleDto vehicleDto) {
    VehicleDto created = vehicleApi.createVehicle(vehicleDto);
    return ResponseEntity.created(URI.create("/api/v1/vehicles/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update vehicle details")
  public ResponseEntity<VehicleDto> updateVehicle(
      @PathVariable final String id, @Valid @RequestBody final VehicleDto vehicleDto) {
    return ResponseEntity.ok(vehicleApi.updateVehicle(id, vehicleDto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Deregister/delete vehicle")
  public ResponseEntity<Void> deleteVehicle(@PathVariable final String id) {
    vehicleApi.deleteVehicle(id);
    return ResponseEntity.noContent().build();
  }

  private String resolveUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    String name = authentication.getName();
    return userApi
        .findByUsername(name)
        .or(() -> userApi.findByEmail(name))
        .or(() -> userApi.findByKeycloakId(name))
        .map(UserDto::id)
        .orElse(name);
  }
}
