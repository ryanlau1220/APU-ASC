package com.apu.asc.vehicle.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
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
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all registered vehicles")
  public ResponseEntity<List<VehicleDto>> getAllVehicles() {
    return ResponseEntity.ok(vehicleApi.findAllVehicles());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get vehicle by ID")
  public ResponseEntity<VehicleDto> getVehicleById(
      @PathVariable final String id, Authentication authentication) {
    VehicleDto vehicle = vehicleApi.getVehicleById(id);
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    accessPolicy.requireSelfOrOperational(currentUser, vehicle.customerId());
    return ResponseEntity.ok(vehicle);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my registered vehicles")
  public ResponseEntity<List<VehicleDto>> getMyVehicles(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(vehicleApi.findVehiclesByCustomer(currentUser.id()));
  }

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get vehicles by customer ID")
  public ResponseEntity<List<VehicleDto>> getVehiclesByCustomer(
      @PathVariable final String customerId, Authentication authentication) {
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), customerId);
    return ResponseEntity.ok(vehicleApi.findVehiclesByCustomer(customerId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Register vehicle")
  public ResponseEntity<VehicleDto> createVehicle(
      @Valid @RequestBody final VehicleDto vehicleDto, Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    VehicleDto securedVehicle = vehicleDto;
    if (!accessPolicy.isOperationalUser(currentUser)) {
      securedVehicle =
          new VehicleDto(
              null,
              currentUser.id(),
              vehicleDto.licensePlate(),
              vehicleDto.make(),
              vehicleDto.model(),
              vehicleDto.yearOfManufacture(),
              null);
    }
    VehicleDto created = vehicleApi.createVehicle(securedVehicle);
    return ResponseEntity.created(URI.create("/api/v1/vehicles/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update vehicle details")
  public ResponseEntity<VehicleDto> updateVehicle(
      @PathVariable final String id,
      @Valid @RequestBody final VehicleDto vehicleDto,
      Authentication authentication) {
    VehicleDto existing = vehicleApi.getVehicleById(id);
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    accessPolicy.requireSelfOrOperational(currentUser, existing.customerId());

    VehicleDto securedVehicle = vehicleDto;
    if (!accessPolicy.isOperationalUser(currentUser)) {
      securedVehicle =
          new VehicleDto(
              existing.id(),
              existing.customerId(),
              vehicleDto.licensePlate(),
              vehicleDto.make(),
              vehicleDto.model(),
              vehicleDto.yearOfManufacture(),
              existing.createdAt());
    }
    return ResponseEntity.ok(vehicleApi.updateVehicle(id, securedVehicle));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Deregister/delete vehicle")
  public ResponseEntity<Void> deleteVehicle(
      @PathVariable final String id, Authentication authentication) {
    VehicleDto vehicle = vehicleApi.getVehicleById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), vehicle.customerId());
    vehicleApi.deleteVehicle(id);
    return ResponseEntity.noContent().build();
  }
}
