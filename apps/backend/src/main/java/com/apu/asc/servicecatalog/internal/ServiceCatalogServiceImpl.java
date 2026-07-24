package com.apu.asc.servicecatalog.internal;

import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ServiceCatalogServiceImpl implements ServiceCatalogApi {

  private final CategoryRepository categoryRepository;
  private final ServiceRepository serviceRepository;

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDto> findAllCategories() {
    return categoryRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public CategoryDto createCategory(final CategoryDto categoryDto) {
    String id =
        categoryDto.id() != null
            ? categoryDto.id()
            : "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    CategoryEntity entity =
        CategoryEntity.builder()
            .id(id)
            .name(categoryDto.name())
            .description(categoryDto.description())
            .build();
    return toDto(categoryRepository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceDto> findAllServices() {
    return serviceRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ServiceDto getServiceById(final String id) {
    return serviceRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
  }

  @Override
  @Transactional
  public ServiceDto createService(final ServiceDto serviceDto) {
    String id =
        serviceDto.id() != null
            ? serviceDto.id()
            : "SVC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    ServiceEntity entity =
        ServiceEntity.builder()
            .id(id)
            .categoryId(serviceDto.categoryId())
            .name(serviceDto.name())
            .description(serviceDto.description())
            .durationMinutes(
                serviceDto.durationMinutes() != null ? serviceDto.durationMinutes() : 60)
            .basePrice(serviceDto.basePrice())
            .status(serviceDto.status() != null ? serviceDto.status() : "ACTIVE")
            .build();
    return toDto(serviceRepository.save(entity));
  }

  private CategoryDto toDto(CategoryEntity entity) {
    return new CategoryDto(
        entity.getId(), entity.getName(), entity.getDescription(), entity.getCreatedAt());
  }

  private ServiceDto toDto(ServiceEntity entity) {
    return new ServiceDto(
        entity.getId(),
        entity.getCategoryId(),
        entity.getName(),
        entity.getDescription(),
        entity.getDurationMinutes(),
        entity.getBasePrice(),
        entity.getStatus(),
        entity.getCreatedAt());
  }
}
