package com.apu.asc.servicecatalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<CategoryEntity> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public CategoryEntity createCategory(CategoryEntity category) {
        if (category.getId() == null) {
            category.setId("CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> findAllServices() {
        return serviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ServiceEntity getServiceById(String id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
    }

    @Transactional
    public ServiceEntity createService(ServiceEntity service) {
        if (service.getId() == null) {
            service.setId("SVC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return serviceRepository.save(service);
    }
}
