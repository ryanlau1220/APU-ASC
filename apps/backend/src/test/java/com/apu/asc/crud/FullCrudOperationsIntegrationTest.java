package com.apu.asc.crud;

import static org.assertj.core.api.Assertions.assertThat;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDecisionRequestDto;
import com.apu.asc.quotation.QuotationDraftRequestDto;
import com.apu.asc.quotation.QuotationLineRequestDto;
import com.apu.asc.servicecatalog.CategoryDto;
import com.apu.asc.servicecatalog.ServiceCatalogApi;
import com.apu.asc.servicecatalog.ServiceDto;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.user.internal.EmailService;
import com.apu.asc.user.internal.KeycloakAdminService;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  @Autowired private WorkOrderApi workOrderApi;
  @Autowired private QuotationApi quotationApi;
  @MockBean private EmailService emailService;
  @MockBean private KeycloakAdminService keycloakAdminService;

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

  private UserDto createTestCustomer() {
    return createTestUserWithRole("CUSTOMER");
  }

  private UserDto createTestUserWithRole(String role) {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    UserDto u =
        userApi.createUser(
            new UserDto(
                null,
                "kc-" + suffix,
                "user_" + suffix,
                "user_" + suffix + "@apu-asc.com",
                "Test " + role,
                role,
                "ACTIVE",
                null,
                null,
                null));
    createdUsers.add(u.id());
    return u;
  }

  private VehicleDto createTestVehicle(String customerId) {
    VehicleDto v =
        vehicleApi.createVehicle(
            new VehicleDto(
                null,
                customerId,
                "WXX-" + System.currentTimeMillis() % 10000,
                "Toyota",
                "Camry",
                2022,
                null));
    createdVehicles.add(v.id());
    return v;
  }

  private ServiceDto createTestService() {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    CategoryDto cat =
        serviceCatalogApi.createCategory(
            new CategoryDto(null, "Test Category " + suffix, "Desc", null));
    createdCategories.add(cat.id());
    ServiceDto s =
        serviceCatalogApi.createService(
            new ServiceDto(
                null,
                cat.id(),
                "Test Service " + suffix,
                "Desc",
                60,
                BigDecimal.valueOf(100.00),
                "ACTIVE",
                null));
    createdServices.add(s.id());
    return s;
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Appointments")
  void testAppointmentCrudLifecycle() {
    UserDto customer = createTestCustomer();
    VehicleDto vehicle = createTestVehicle(customer.id());
    ServiceDto service = createTestService();
    UserDto technician = createTestUserWithRole("TECHNICIAN");

    AppointmentDto newApt =
        new AppointmentDto(
            null,
            customer.id(),
            vehicle.id(),
            service.id(),
            technician.id(),
            LocalDate.now().plusDays(1),
            "09:00 AM",
            "PENDING",
            "Initial inspection",
            null,
            null);

    AppointmentDto created = appointmentApi.createAppointment(newApt);
    createdAppointments.add(created.id());
    assertThat(created.id()).startsWith("APT-");

    AppointmentDto updated =
        appointmentApi.updateAppointment(
            created.id(),
            new AppointmentDto(
                created.id(),
                created.customerId(),
                created.vehicleId(),
                created.serviceId(),
                created.technicianId(),
                created.appointmentDate(),
                created.timeSlot(),
                "CONFIRMED",
                created.notes(),
                null,
                null));
    assertThat(updated.status()).isEqualTo("CONFIRMED");

    appointmentApi.deleteAppointment(created.id());
    createdAppointments.remove(created.id());

    List<AppointmentDto> all = appointmentApi.findAllAppointments();
    assertThat(all.stream().noneMatch(a -> a.id().equals(created.id()))).isTrue();
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Payments & Invoices")
  void testPaymentCrudLifecycle() {
    UserDto customer = createTestCustomer();

    PaymentDto invoice =
        paymentApi.createInvoice(
            new PaymentDto(
                null,
                "APT-99999",
                customer.id(),
                "INV-99999",
                BigDecimal.valueOf(250.00),
                "CREDIT_CARD",
                "UNPAID",
                null,
                null,
                null));

    assertThat(invoice.id()).startsWith("PAY-");

    PaymentDto processed = paymentApi.processPayment(invoice.id(), "CREDIT_CARD");
    assertThat(processed.paymentStatus()).isEqualTo("PAID");
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Service Catalog & Categories")
  void testServiceCatalogCrudLifecycle() {
    CategoryDto cat =
        serviceCatalogApi.createCategory(
            new CategoryDto(null, "Engine Repairs", "Engine services", null));
    createdCategories.add(cat.id());

    ServiceDto service =
        serviceCatalogApi.createService(
            new ServiceDto(
                null,
                cat.id(),
                "Spark Plug Replacement",
                "Replace 4 spark plugs",
                45,
                BigDecimal.valueOf(150.00),
                "ACTIVE",
                null));
    createdServices.add(service.id());

    serviceCatalogApi.deleteService(service.id());
    createdServices.remove(service.id());
    assertThat(
            serviceCatalogApi.findAllServices().stream()
                .filter(s -> s.status().equals("ACTIVE"))
                .noneMatch(s -> s.id().equals(service.id())))
        .isTrue();
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Users")
  void testUserCrudLifecycle() {
    UserDto user = createTestUserWithRole("STAFF");

    UserDto updatedStatus =
        userApi.updateUser(
            user.id(),
            new UserDto(
                user.id(),
                user.keycloakId(),
                user.username(),
                user.email(),
                user.fullName(),
                user.role(),
                "INACTIVE",
                user.avatarUrl(),
                null,
                null));
    assertThat(updatedStatus.status()).isEqualTo("INACTIVE");
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Vehicles")
  void testVehicleCrudLifecycle() {
    UserDto customer = createTestCustomer();

    VehicleDto vehicle =
        vehicleApi.createVehicle(
            new VehicleDto(null, customer.id(), "WYY-9988", "Honda", "Civic Type R", 2023, null));
    createdVehicles.add(vehicle.id());

    VehicleDto updated =
        vehicleApi.updateVehicle(
            vehicle.id(),
            new VehicleDto(
                vehicle.id(),
                customer.id(),
                "WYY-9988",
                "Honda",
                "Civic Type R (Tuned)",
                2023,
                null));

    assertThat(updated.model()).contains("Tuned");

    vehicleApi.deleteVehicle(vehicle.id());
    createdVehicles.remove(vehicle.id());
  }

  @Test
  @DisplayName("Full CRUD Lifecycle Test for Feedback")
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
                "PENDING",
                "Done",
                null,
                null));
    createdAppointments.add(appointment.id());
    appointment = appointmentApi.updateStatus(appointment.id(), "CONFIRMED");

    WorkOrderDto workOrder =
        workOrderApi.createWorkOrder(
            new WorkOrderDto(
                null,
                appointment.id(),
                customer.id(),
                vehicle.id(),
                service.id(),
                technician.id(),
                "OPEN",
                "Done",
                null,
                null,
                null,
                null,
                null,
                null));
    workOrder = workOrderApi.updateStatus(workOrder.id(), "DIAGNOSING");
    var quotation =
        quotationApi.createDraft(
            customer.id(),
            new QuotationDraftRequestDto(
                workOrder.id(),
                "Oil service estimate",
                LocalDate.now().plusDays(7),
                List.of(
                    new QuotationLineRequestDto(
                        "Engine oil and filter", BigDecimal.ONE, new BigDecimal("120.00")))));
    quotationApi.submit(quotation.id());
    quotationApi.decide(
        quotation.id(),
        new QuotationDecisionRequestDto(QuotationDecisionRequestDto.Decision.APPROVE, null));
    workOrder = workOrderApi.updateStatus(workOrder.id(), "IN_PROGRESS");
    workOrder = workOrderApi.updateStatus(workOrder.id(), "COMPLETED");

    FeedbackDto fbDto =
        new FeedbackDto(
            null,
            appointment.id(),
            customer.id(),
            technician.id(),
            5,
            "Excellent service!",
            "Replaced oil filter",
            null,
            workOrder.id());

    FeedbackDto created = feedbackApi.submitFeedback(fbDto);
    assertThat(created.id()).startsWith("FBK-");

    FeedbackDto fetched = feedbackApi.getFeedbackById(created.id());
    assertThat(fetched.comments()).isEqualTo("Excellent service!");

    feedbackApi.deleteFeedback(created.id());
    assertThat(feedbackApi.findAllFeedbacks().stream().noneMatch(f -> f.id().equals(created.id())))
        .isTrue();
  }
}
