package com.apu.asc.crud;

import static org.assertj.core.api.Assertions.assertThat;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class FullCrudOperationsIntegrationTest {

  @Autowired private AppointmentApi appointmentApi;
  @Autowired private VehicleApi vehicleApi;
  @Autowired private UserApi userApi;
  @Autowired private ServiceCatalogApi serviceCatalogApi;
  @Autowired private PaymentApi paymentApi;
  @Autowired private FeedbackApi feedbackApi;

  private UserDto createTestCustomer() {
    return createTestUserWithRole("CUSTOMER");
  }

  private UserDto createTestUserWithRole(String role) {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    return userApi.createUser(
        new UserDto(
            null,
            "kc-" + suffix,
            "user_" + suffix,
            "user_" + suffix + "@apu-asc.com",
            "Test " + role,
            role,
            "ACTIVE",
            null,
            null));
  }

  private VehicleDto createTestVehicle(String customerId) {
    return vehicleApi.createVehicle(
        new VehicleDto(
            null,
            customerId,
            "WXX-" + System.currentTimeMillis() % 10000,
            "Toyota",
            "Camry",
            2022,
            null));
  }

  private ServiceDto createTestService() {
    CategoryDto cat =
        serviceCatalogApi.createCategory(new CategoryDto(null, "Test Category", "Desc", null));
    return serviceCatalogApi.createService(
        new ServiceDto(
            null,
            cat.id(),
            "Test Service",
            "Desc",
            60,
            BigDecimal.valueOf(100.00),
            "ACTIVE",
            null));
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Appointments")
  void testAppointmentCrudLifecycle() {
    UserDto customer = createTestCustomer();
    VehicleDto vehicle = createTestVehicle(customer.id());
    ServiceDto service = createTestService();

    AppointmentDto newDto =
        new AppointmentDto(
            null,
            customer.id(),
            vehicle.id(),
            service.id(),
            null,
            LocalDate.parse("2026-08-10"),
            "10:00 AM",
            "PENDING",
            "Oil change required",
            null,
            null);

    AppointmentDto created = appointmentApi.createAppointment(newDto);
    assertThat(created.id()).startsWith("APT-");

    AppointmentDto fetched = appointmentApi.getAppointmentById(created.id());
    assertThat(fetched.notes()).isEqualTo("Oil change required");

    AppointmentDto updatedStatus = appointmentApi.updateStatus(created.id(), "COMPLETED");
    assertThat(updatedStatus.status()).isEqualTo("COMPLETED");

    appointmentApi.deleteAppointment(created.id());
    assertThat(
            appointmentApi.findAllAppointments().stream()
                .noneMatch(a -> a.id().equals(created.id())))
        .isTrue();
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Vehicles")
  void testVehicleCrudLifecycle() {
    UserDto customer = createTestCustomer();

    VehicleDto newDto =
        new VehicleDto(null, customer.id(), "WXX9999", "Toyota", "Camry", 2022, null);

    VehicleDto created = vehicleApi.createVehicle(newDto);
    assertThat(created.id()).startsWith("VEH-");

    VehicleDto fetched = vehicleApi.getVehicleById(created.id());
    assertThat(fetched.licensePlate()).isEqualTo("WXX9999");

    VehicleDto updated =
        vehicleApi.updateVehicle(
            created.id(),
            new VehicleDto(
                created.id(), customer.id(), "WXX9999", "Toyota", "Camry Hybrid", 2023, null));
    assertThat(updated.model()).isEqualTo("Camry Hybrid");

    vehicleApi.deleteVehicle(created.id());
    assertThat(vehicleApi.findAllVehicles().stream().noneMatch(v -> v.id().equals(created.id())))
        .isTrue();
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Users")
  void testUserCrudLifecycle() {
    UserDto created = createTestCustomer();
    assertThat(created.id()).startsWith("USR-");

    UserDto fetched = userApi.getUserById(created.id());
    assertThat(fetched.fullName()).isEqualTo("Test CUSTOMER");

    UserDto updatedStatus = userApi.updateStatus(created.id(), "INACTIVE");
    assertThat(updatedStatus.status()).isEqualTo("INACTIVE");
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Service Catalog")
  void testServiceCatalogCrudLifecycle() {
    CategoryDto catDto = new CategoryDto(null, "Engine Services", "Engine maintenance", null);
    CategoryDto createdCat = serviceCatalogApi.createCategory(catDto);
    assertThat(createdCat.id()).startsWith("CAT-");

    ServiceDto svcDto =
        new ServiceDto(
            null,
            createdCat.id(),
            "Full Engine Diagnostic",
            "Complete scan",
            60,
            BigDecimal.valueOf(150.00),
            "ACTIVE",
            null);

    ServiceDto createdSvc = serviceCatalogApi.createService(svcDto);
    assertThat(createdSvc.id()).startsWith("SVC-");

    ServiceDto fetchedSvc = serviceCatalogApi.getServiceById(createdSvc.id());
    assertThat(fetchedSvc.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(150.00));

    serviceCatalogApi.deleteService(createdSvc.id());
    ServiceDto deactivated = serviceCatalogApi.getServiceById(createdSvc.id());
    assertThat(deactivated.status()).isEqualTo("INACTIVE");
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Payments & Invoicing")
  void testPaymentCrudLifecycle() {
    UserDto customer = createTestCustomer();

    PaymentDto payDto =
        new PaymentDto(
            null,
            "APT-100",
            customer.id(),
            "INV-2001",
            BigDecimal.valueOf(250.00),
            "CREDIT_CARD",
            "UNPAID",
            null,
            null);

    PaymentDto created = paymentApi.createInvoice(payDto);
    assertThat(created.id()).startsWith("PAY-");

    PaymentDto processed = paymentApi.processPayment(created.id(), "CREDIT_CARD");
    assertThat(processed.paymentStatus()).isEqualTo("PAID");
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Feedback & Reviews")
  void testFeedbackCrudLifecycle() {
    UserDto customer = createTestCustomer();
    UserDto technician = createTestUserWithRole("TECHNICIAN");
    VehicleDto vehicle = createTestVehicle(customer.id());
    ServiceDto service = createTestService();

    AppointmentDto appointment =
        appointmentApi.createAppointment(
            new AppointmentDto(
                null,
                customer.id(),
                vehicle.id(),
                service.id(),
                technician.id(),
                LocalDate.parse("2026-08-10"),
                "10:00 AM",
                "COMPLETED",
                "Done",
                null,
                null));

    FeedbackDto fbDto =
        new FeedbackDto(
            null,
            appointment.id(),
            customer.id(),
            technician.id(),
            5,
            "Excellent service!",
            "Replaced oil filter",
            null);

    FeedbackDto created = feedbackApi.submitFeedback(fbDto);
    assertThat(created.id()).startsWith("FBK-");

    FeedbackDto fetched = feedbackApi.getFeedbackById(created.id());
    assertThat(fetched.comments()).isEqualTo("Excellent service!");

    feedbackApi.deleteFeedback(created.id());
    assertThat(feedbackApi.findAllFeedbacks().stream().noneMatch(f -> f.id().equals(created.id())))
        .isTrue();
  }
}
