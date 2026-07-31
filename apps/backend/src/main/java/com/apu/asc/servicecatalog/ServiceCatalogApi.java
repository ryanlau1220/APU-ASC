package com.apu.asc.servicecatalog;

import java.util.List;

public interface ServiceCatalogApi {
  List<CategoryDto> findAllCategories();

  CategoryDto getCategoryById(String id);

  CategoryDto createCategory(CategoryDto categoryDto);

  CategoryDto updateCategory(String id, CategoryDto categoryDto);

  void deleteCategory(String id);

  List<ServiceDto> findAllServices();

  ServiceDto getServiceById(String id);

  ServiceDto createService(ServiceDto serviceDto);

  ServiceDto updateService(String id, ServiceDto serviceDto);

  void deleteService(String id);
}
