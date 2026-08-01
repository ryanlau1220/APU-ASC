package com.apu.asc.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class EventDrivenAuditLogIntegrationTest {

  @Autowired private ApplicationEventPublisher eventPublisher;
  @Autowired private AuditLogApi auditLogApi;
  @Autowired private UserApi userApi;
  @Autowired private VehicleApi vehicleApi;
  @Autowired private ServiceCatalogApi serviceCatalogApi;
  @Autowired private AppointmentApi appointmentApi;

  private final List<String> createdAppointments = new ArrayList<>();
  private final List<String> createdServices = new ArrayList<>();
  private final List<String> createdCategories = new ArrayList<>();
  private final List<String> createdVehicles = new ArrayList<>();
  private final List<String> createdUsers = new ArrayList<>();

  @AfterEach
  void tearDown() {
    createdAppointments.forEach(
        id -> {
          try {
            appointmentApi.deleteAppointment(id);
          } catch (Exception ignored) {
          }
        });
    createdServices.forEach(
        id -> {
          try {
            serviceCatalogApi.deleteService(id);
          } catch (Exception ignored) {
          }
        });
    createdCategories.forEach(
        id -> {
          try {
            serviceCatalogApi.deleteCategory(id);
          } catch (Exception ignored) {
          }
        });
    createdVehicles.forEach(
        id -> {
          try {
            vehicleApi.deleteVehicle(id);
          } catch (Exception ignored) {
          }
        });
    createdUsers.forEach(
        id -> {
          try {
            userApi.hardDeleteUser(id);
          } catch (Exception ignored) {
          }
        });

    createdAppointments.clear();
    createdServices.clear();
    createdCategories.clear();
    createdVehicles.clear();
    createdUsers.clear();
  }

  @Test
  @DisplayName("Direct AuditEvent publishing should persist audit record in partitioned database")
  void testDirectAuditEventPublishing() {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    UserDto user =
        userApi.createUser(
            new UserDto(
                null,
                "kc-direct-" + suffix,
                "audit_direct_" + suffix,
                "audit_direct_" + suffix + "@apu-asc.com",
                "Direct Audit User",
                "CUSTOMER",
                "ACTIVE",
                null,
                null));
    createdUsers.add(user.id());

    eventPublisher.publishEvent(
        new AuditEvent(
            user.id(), "MANUAL_TEST_ACTION", "USER", user.id(), "Manual audit test log"));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              List<AuditLogDto> logs = auditLogApi.findByUserId(user.id());
              assertThat(logs).isNotEmpty();
              assertThat(logs.stream().anyMatch(l -> l.actionType().equals("MANUAL_TEST_ACTION")))
                  .isTrue();
            });
  }

  @Test
  @DisplayName(
      "Domain mutations in User, Vehicle, and Appointment should generate audit log events")
  void testDomainMutationsGenerateAuditLogs() {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    UserDto user =
        userApi.createUser(
            new UserDto(
                null,
                "kc-" + suffix,
                "audit_user_" + suffix,
                "audit_" + suffix + "@apu-asc.com",
                "Audit Test User",
                "CUSTOMER",
                "ACTIVE",
                null,
                null));
    createdUsers.add(user.id());

    VehicleDto vehicle =
        vehicleApi.createVehicle(
            new VehicleDto(
                null, user.id(), "WYY-" + suffix.toUpperCase(), "Honda", "Civic", 2023, null));
    createdVehicles.add(vehicle.id());

    CategoryDto cat =
        serviceCatalogApi.createCategory(
            new CategoryDto(null, "Brake Service " + suffix, "Brake check", null));
    createdCategories.add(cat.id());

    ServiceDto svc =
        serviceCatalogApi.createService(
            new ServiceDto(
                null,
                cat.id(),
                "Brake Fluid Flush " + suffix,
                "Flush fluid",
                45,
                BigDecimal.valueOf(120.0),
                "ACTIVE",
                null));
    createdServices.add(svc.id());

    AppointmentDto appointment =
        appointmentApi.createAppointment(
            new AppointmentDto(
                null,
                user.id(),
                vehicle.id(),
                svc.id(),
                null,
                LocalDate.now().plusDays(2),
                "02:00 PM",
                "PENDING",
                "Check brakes",
                null,
                null));
    createdAppointments.add(appointment.id());

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              List<AuditLogDto> userLogs = auditLogApi.findByUserId(user.id());
              assertThat(userLogs).isNotEmpty();

              List<AuditLogDto> apptLogs = auditLogApi.findByEntityName("APPOINTMENT");
              assertThat(apptLogs.stream().anyMatch(l -> l.details().contains(appointment.id())))
                  .isTrue();
            });
  }
}
