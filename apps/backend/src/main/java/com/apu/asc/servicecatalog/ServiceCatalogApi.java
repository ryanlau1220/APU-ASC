package com.apu.asc.servicecatalog;

import java.util.List;

public interface ServiceCatalogApi {
  List<CategoryDto> findAllCategories();

  CategoryDto createCategory(CategoryDto categoryDto);

  List<ServiceDto> findAllServices();

  ServiceDto getServiceById(String id);

  ServiceDto createService(ServiceDto serviceDto);
}
