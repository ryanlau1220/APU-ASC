package com.apu.asc.appointment;

import java.util.List;

public interface AppointmentApi {
  List<AppointmentDto> findAllAppointments();

  List<AppointmentDto> findByCustomer(String customerId);

  List<AppointmentDto> findByTechnician(String technicianId);

  AppointmentDto createAppointment(AppointmentDto appointmentDto);
}
