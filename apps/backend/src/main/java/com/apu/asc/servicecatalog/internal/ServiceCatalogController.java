package com.apu.asc.servicecatalog.internal;

import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @GetMapping("/categories/{id}")
  @Operation(summary = "Get category by ID")
  public ResponseEntity<CategoryDto> getCategoryById(@PathVariable final String id) {
    return ResponseEntity.ok(serviceCatalogApi.getCategoryById(id));
  }

  @PostMapping("/categories")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Create category")
  public ResponseEntity<CategoryDto> createCategory(
      @Valid @RequestBody final CategoryDto categoryDto) {
    CategoryDto created = serviceCatalogApi.createCategory(categoryDto);
    return ResponseEntity.created(URI.create("/api/v1/catalog/categories/" + created.id()))
        .body(created);
  }

  @PutMapping("/categories/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Update category")
  public ResponseEntity<CategoryDto> updateCategory(
      @PathVariable final String id, @Valid @RequestBody final CategoryDto categoryDto) {
    return ResponseEntity.ok(serviceCatalogApi.updateCategory(id, categoryDto));
  }

  @DeleteMapping("/categories/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Delete category")
  public ResponseEntity<Void> deleteCategory(@PathVariable final String id) {
    serviceCatalogApi.deleteCategory(id);
    return ResponseEntity.noContent().build();
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
  public ResponseEntity<ServiceDto> createService(@Valid @RequestBody final ServiceDto serviceDto) {
    ServiceDto created = serviceCatalogApi.createService(serviceDto);
    return ResponseEntity.created(URI.create("/api/v1/catalog/services/" + created.id()))
        .body(created);
  }

  @PutMapping("/services/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Update service")
  public ResponseEntity<ServiceDto> updateService(
      @PathVariable final String id, @Valid @RequestBody final ServiceDto serviceDto) {
    return ResponseEntity.ok(serviceCatalogApi.updateService(id, serviceDto));
  }

  @DeleteMapping("/services/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Deactivate service")
  public ResponseEntity<Void> deleteService(@PathVariable final String id) {
    serviceCatalogApi.deleteService(id);
    return ResponseEntity.noContent().build();
  }
}
