package com.apu.asc.dao;

import com.apu.asc.model.Payment;
import java.util.List;

public interface IPaymentDAO {
  public void reload();
  public void save(Payment payment);
  public Payment findByAppointment(String appointmentId);
  public List<Payment> getAll();
}
