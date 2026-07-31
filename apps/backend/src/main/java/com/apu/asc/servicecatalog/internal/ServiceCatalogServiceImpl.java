package com.apu.asc.servicecatalog.internal;

import com.apu.asc.common.exception.ResourceNotFoundException;
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
  @Transactional(readOnly = true)
  public CategoryDto getCategoryById(final String id) {
    return categoryRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
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
  @Transactional
  public CategoryDto updateCategory(final String id, final CategoryDto categoryDto) {
    CategoryEntity entity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

    if (categoryDto.name() != null) entity.setName(categoryDto.name());
    if (categoryDto.description() != null) entity.setDescription(categoryDto.description());

    return toDto(categoryRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteCategory(final String id) {
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Category", id);
    }
    categoryRepository.deleteById(id);
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
        .orElseThrow(() -> new ResourceNotFoundException("Service", id));
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

  @Override
  @Transactional
  public ServiceDto updateService(final String id, final ServiceDto serviceDto) {
    ServiceEntity entity =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));

    if (serviceDto.categoryId() != null) entity.setCategoryId(serviceDto.categoryId());
    if (serviceDto.name() != null) entity.setName(serviceDto.name());
    if (serviceDto.description() != null) entity.setDescription(serviceDto.description());
    if (serviceDto.durationMinutes() != null)
      entity.setDurationMinutes(serviceDto.durationMinutes());
    if (serviceDto.basePrice() != null) entity.setBasePrice(serviceDto.basePrice());
    if (serviceDto.status() != null) entity.setStatus(serviceDto.status());

    return toDto(serviceRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteService(final String id) {
    ServiceEntity entity =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    entity.setStatus("INACTIVE");
    serviceRepository.save(entity);
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
