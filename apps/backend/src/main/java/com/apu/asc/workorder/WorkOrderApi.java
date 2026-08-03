package com.apu.asc.workorder;

import java.util.List;
import java.util.Optional;

public interface WorkOrderApi {
  List<WorkOrderDto> findAllWorkOrders();

  WorkOrderDto getWorkOrderById(String id);

  Optional<WorkOrderDto> findByAppointmentId(String appointmentId);

  List<WorkOrderDto> findByCustomer(String customerId);

  List<WorkOrderDto> findByTechnician(String technicianId);

  WorkOrderDto createWorkOrder(WorkOrderDto workOrderDto);

  WorkOrderDto updateWorkOrder(String id, WorkOrderDto workOrderDto);

  WorkOrderDto updateStatus(String id, String status);

  WorkOrderDto updateDiagnosticNotes(String id, String diagnosticNotes);

  void deleteWorkOrder(String id);
}
