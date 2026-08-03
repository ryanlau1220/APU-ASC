package com.apu.asc.servicecatalog.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import com.apu.asc.servicecatalog.ServiceStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ServiceCatalogServiceImpl implements ServiceCatalogApi {

  private final CategoryRepository categoryRepository;
  private final ServiceRepository serviceRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "categories")
  public List<CategoryDto> findAllCategories() {
    return categoryRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "categories", key = "#id")
  public CategoryDto getCategoryById(final String id) {
    return categoryRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Category", id));
  }

  @Override
  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  public CategoryDto createCategory(final CategoryDto categoryDto) {
    String id = categoryDto.id() != null ? categoryDto.id() : "CAT-" + UUID.randomUUID().toString();
    CategoryEntity entity =
        CategoryEntity.builder()
            .id(id)
            .name(categoryDto.name())
            .description(categoryDto.description())
            .build();
    CategoryDto created = toDto(categoryRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "CATEGORY_CREATED",
            "CATEGORY",
            created.id(),
            "Created catalog category: " + created.name()));
    return created;
  }

  @Override
  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  public CategoryDto updateCategory(final String id, final CategoryDto categoryDto) {
    CategoryEntity entity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));

    if (categoryDto.name() != null) entity.setName(categoryDto.name());
    if (categoryDto.description() != null) entity.setDescription(categoryDto.description());

    CategoryDto updated = toDto(categoryRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "CATEGORY_UPDATED",
            "CATEGORY",
            updated.id(),
            "Updated catalog category: " + updated.name()));
    return updated;
  }

  @Override
  @Transactional
  @CacheEvict(
      value = {"categories", "services"},
      allEntries = true)
  public void deleteCategory(final String id) {
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Category", id);
    }
    serviceRepository.deleteAll(serviceRepository.findByCategoryId(id));
    categoryRepository.deleteById(id);
    eventPublisher.publishEvent(
        new AuditEvent("SYSTEM", "CATEGORY_DELETED", "CATEGORY", id, "Deleted catalog category"));
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "services")
  public List<ServiceDto> findAllServices() {
    return serviceRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "services", key = "#id")
  public ServiceDto getServiceById(final String id) {
    return serviceRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Service", id));
  }

  @Override
  @Transactional
  @CacheEvict(value = "services", allEntries = true)
  public ServiceDto createService(final ServiceDto serviceDto) {
    String id = serviceDto.id() != null ? serviceDto.id() : "SVC-" + UUID.randomUUID().toString();
    ServiceEntity entity =
        ServiceEntity.builder()
            .id(id)
            .categoryId(serviceDto.categoryId())
            .name(serviceDto.name())
            .description(serviceDto.description())
            .durationMinutes(
                serviceDto.durationMinutes() != null ? serviceDto.durationMinutes() : 60)
            .basePrice(serviceDto.basePrice())
            .status(
                serviceDto.status() == null || serviceDto.status().isBlank()
                    ? ServiceStatus.ACTIVE.name()
                    : ServiceStatus.fromString(serviceDto.status()).name())
            .build();
    ServiceDto created = toDto(serviceRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "SERVICE_CREATED",
            "SERVICE",
            created.id(),
            "Created service offering: " + created.name() + " (" + created.basePrice() + ")"));
    return created;
  }

  @Override
  @Transactional
  @CacheEvict(value = "services", allEntries = true)
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
    if (serviceDto.status() != null) {
      ServiceStatus current = ServiceStatus.fromString(entity.getStatus());
      ServiceStatus target = ServiceStatus.fromString(serviceDto.status());
      current.requireTransitionTo(target);
      entity.setStatus(target.name());
    }

    ServiceDto updated = toDto(serviceRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "SERVICE_UPDATED",
            "SERVICE",
            updated.id(),
            "Updated service details for " + updated.name()));
    return updated;
  }

  @Override
  @Transactional
  @CacheEvict(value = "services", allEntries = true)
  public void deleteService(final String id) {
    ServiceEntity entity =
        serviceRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    ServiceStatus current = ServiceStatus.fromString(entity.getStatus());
    current.requireTransitionTo(ServiceStatus.INACTIVE);
    entity.setStatus(ServiceStatus.INACTIVE.name());
    serviceRepository.save(entity);
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "SERVICE_DEACTIVATED",
            "SERVICE",
            id,
            "Deactivated service offering " + entity.getName()));
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
