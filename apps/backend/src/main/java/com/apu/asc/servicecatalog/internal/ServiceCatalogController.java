package com.apu.asc.servicecatalog.internal;

import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
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
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Service Catalog", description = "Service and Category management APIs")
class ServiceCatalogController {

  private final ServiceCatalogApi serviceCatalogApi;

  @GetMapping("/categories")
  @Operation(summary = "Get all categories")
  public ResponseEntity<List<CategoryDto>> getCategories() {
    return ResponseEntity.ok(serviceCatalogApi.findAllCategories());
  }

  @PostMapping("/categories")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Create category")
  public ResponseEntity<CategoryDto> createCategory(@RequestBody final CategoryDto categoryDto) {
    return ResponseEntity.ok(serviceCatalogApi.createCategory(categoryDto));
  }

  @GetMapping("/services")
  @Operation(summary = "Get all services")
  public ResponseEntity<List<ServiceDto>> getServices() {
    return ResponseEntity.ok(serviceCatalogApi.findAllServices());
  }

  @GetMapping("/services/{id}")
  @Operation(summary = "Get service by ID")
  public ResponseEntity<ServiceDto> getServiceById(@PathVariable final String id) {
    return ResponseEntity.ok(serviceCatalogApi.getServiceById(id));
  }

  @PostMapping("/services")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Create service")
  public ResponseEntity<ServiceDto> createService(@RequestBody final ServiceDto serviceDto) {
    return ResponseEntity.ok(serviceCatalogApi.createService(serviceDto));
  }
}
