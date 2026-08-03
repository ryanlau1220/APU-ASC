package com.apu.asc.scheduling.internal;

import com.apu.asc.scheduling.SchedulingApi;
import com.apu.asc.scheduling.SlotAvailabilityDto;
import com.apu.asc.scheduling.SlotCapacityUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scheduling")
@RequiredArgsConstructor
@Tag(name = "Scheduling", description = "Workshop slot availability and capacity controls")
class SchedulingController {

  private final SchedulingApi schedulingApi;

  @GetMapping("/availability")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'TECHNICIAN', 'MANAGER')")
  @Operation(
      operationId = "getSlotAvailability",
      summary = "Get workshop capacity for an appointment date")
  ResponseEntity<List<SlotAvailabilityDto>> getAvailability(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(schedulingApi.getAvailability(date));
  }

  @PutMapping("/capacity")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(
      operationId = "updateSlotCapacity",
      summary = "Set the capacity for one workshop appointment slot")
  ResponseEntity<SlotAvailabilityDto> updateCapacity(
      @Valid @RequestBody SlotCapacityUpdateDto update) {
    return ResponseEntity.ok(schedulingApi.updateCapacity(update));
  }
}
