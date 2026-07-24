package com.apu.asc.vehicle.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface VehicleRepository extends JpaRepository<VehicleEntity, String> {
  List<VehicleEntity> findByCustomerId(String customerId);
}
