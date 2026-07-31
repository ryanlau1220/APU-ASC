package com.apu.asc.appointment;

import java.util.List;

public interface AppointmentApi {
  List<AppointmentDto> findAllAppointments();

  AppointmentDto getAppointmentById(String id);

  List<AppointmentDto> findByCustomer(String customerId);

  List<AppointmentDto> findByTechnician(String technicianId);

  AppointmentDto createAppointment(AppointmentDto appointmentDto);

  AppointmentDto updateAppointment(String id, AppointmentDto appointmentDto);

  AppointmentDto updateStatus(String id, String status);

  void deleteAppointment(String id);
}
