package com.apu.asc.servicecatalog.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceImplTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private ServiceRepository serviceRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  private ServiceCatalogServiceImpl serviceCatalogService;

  @BeforeEach
  void setUp() {
    serviceCatalogService =
        new ServiceCatalogServiceImpl(categoryRepository, serviceRepository, eventPublisher);
  }

  @Test
  @DisplayName("Should return all service categories")
  void shouldReturnAllCategories() {
    CategoryEntity category =
        new CategoryEntity("CAT-101", "Regular Maintenance", "Oil service", Instant.now());
    when(categoryRepository.findAll()).thenReturn(List.of(category));

    List<CategoryDto> categories = serviceCatalogService.findAllCategories();

    assertThat(categories).hasSize(1);
    assertThat(categories.get(0).name()).isEqualTo("Regular Maintenance");
  }

  @Test
  @DisplayName("Should return service by ID")
  void shouldReturnServiceById() {
    ServiceEntity service =
        new ServiceEntity(
            "SVC-101",
            "CAT-101",
            "Oil Service",
            "Synthetic oil change",
            60,
            BigDecimal.valueOf(180.00),
            "ACTIVE",
            Instant.now());

    when(serviceRepository.findById("SVC-101")).thenReturn(Optional.of(service));

    ServiceDto result = serviceCatalogService.getServiceById("SVC-101");

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Oil Service");
    assertThat(result.basePrice()).isEqualByComparingTo("180.00");
  }
}
