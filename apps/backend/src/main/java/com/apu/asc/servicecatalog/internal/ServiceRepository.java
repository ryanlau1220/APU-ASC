package com.apu.asc.servicecatalog.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ServiceRepository extends JpaRepository<ServiceEntity, String> {
  List<ServiceEntity> findByCategoryId(String categoryId);
}
