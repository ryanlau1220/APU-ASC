package com.apu.asc.payment.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
  Optional<PaymentEntity> findByAppointmentId(String appointmentId);

  List<PaymentEntity> findByCustomerId(String customerId);
}
