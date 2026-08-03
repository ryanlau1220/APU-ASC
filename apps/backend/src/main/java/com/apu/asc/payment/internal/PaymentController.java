package com.apu.asc.payment.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments & Invoicing", description = "Invoice generation and payment processing APIs")
class PaymentController {

  private final PaymentApi paymentApi;
  private final WorkOrderApi workOrderApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all payments")
  public ResponseEntity<List<PaymentDto>> getAllPayments() {
    return ResponseEntity.ok(paymentApi.findAllPayments());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get payment/invoice by ID")
  public ResponseEntity<PaymentDto> getPaymentById(
      @PathVariable final String id, Authentication authentication) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get my payments/invoices")
  public ResponseEntity<List<PaymentDto>> getMyPayments(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(paymentApi.getByCustomer(currentUser.id()));
  }

  @GetMapping("/appointment/{appointmentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get invoice by appointment ID")
  public ResponseEntity<PaymentDto> getPaymentByAppointment(
      @PathVariable final String appointmentId, Authentication authentication) {
    PaymentDto payment = paymentApi.getByAppointment(appointmentId);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/work-order/{workOrderId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get invoice by work-order ID")
  public ResponseEntity<PaymentDto> getPaymentByWorkOrder(
      @PathVariable final String workOrderId, Authentication authentication) {
    PaymentDto payment = paymentApi.getByWorkOrder(workOrderId);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(payment);
  }

  @PostMapping
  @PreAuthorize("hasRole('STAFF')")
  @Operation(summary = "Create invoice for appointment")
  public ResponseEntity<PaymentDto> createInvoice(
      @Valid @RequestBody final PaymentDto paymentDto, Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    WorkOrderDto workOrder = resolveWorkOrder(paymentDto);
    PaymentDto securedPayment =
        new PaymentDto(
            null,
            workOrder.appointmentId(),
            workOrder.customerId(),
            paymentDto.invoiceNumber(),
            paymentDto.amount(),
            paymentDto.paymentMethod(),
            paymentDto.paymentStatus(),
            null,
            null,
            workOrder.id());
    PaymentDto created = paymentApi.createInvoice(securedPayment);
    return ResponseEntity.created(URI.create("/api/v1/payments/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Update payment/invoice details")
  public ResponseEntity<PaymentDto> updatePayment(
      @PathVariable final String id, @Valid @RequestBody final PaymentDto paymentDto) {
    return ResponseEntity.ok(paymentApi.updatePayment(id, paymentDto));
  }

  @PostMapping("/{id}/pay")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Process invoice payment")
  public ResponseEntity<PaymentDto> processPayment(
      @PathVariable final String id,
      @RequestParam final String method,
      Authentication authentication) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(paymentApi.processPayment(id, method));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Delete invoice/payment record")
  public ResponseEntity<Void> deletePayment(@PathVariable final String id) {
    paymentApi.deletePayment(id);
    return ResponseEntity.noContent().build();
  }

  private WorkOrderDto resolveWorkOrder(PaymentDto paymentDto) {
    if (paymentDto.workOrderId() != null && !paymentDto.workOrderId().isBlank()) {
      return workOrderApi.getWorkOrderById(paymentDto.workOrderId());
    }
    if (paymentDto.appointmentId() != null && !paymentDto.appointmentId().isBlank()) {
      return workOrderApi
          .findByAppointmentId(paymentDto.appointmentId())
          .orElseThrow(
              () ->
                  new IllegalArgumentException("The appointment does not yet have a work order."));
    }
    throw new IllegalArgumentException("An invoice must be associated with a work order.");
  }
}
