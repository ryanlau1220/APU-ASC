package com.apu.asc.servicecatalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Service Catalog", description = "Service and Category management APIs")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryEntity>> getCategories() {
        return ResponseEntity.ok(serviceCatalogService.findAllCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryEntity> createCategory(@RequestBody CategoryEntity category) {
        return ResponseEntity.ok(serviceCatalogService.createCategory(category));
    }

    @GetMapping("/services")
    @Operation(summary = "Get all services")
    public ResponseEntity<List<ServiceEntity>> getServices() {
        return ResponseEntity.ok(serviceCatalogService.findAllServices());
    }

    @GetMapping("/services/{id}")
    @Operation(summary = "Get service by ID")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable String id) {
        return ResponseEntity.ok(serviceCatalogService.getServiceById(id));
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create service")
    public ResponseEntity<ServiceEntity> createService(@RequestBody ServiceEntity service) {
        return ResponseEntity.ok(serviceCatalogService.createService(service));
    }
}
