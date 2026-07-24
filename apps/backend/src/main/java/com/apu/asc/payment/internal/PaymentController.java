package com.apu.asc.payment.internal;

import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all payments")
  public ResponseEntity<List<PaymentDto>> getAllPayments() {
    return ResponseEntity.ok(paymentApi.findAllPayments());
  }

  @GetMapping("/appointment/{appointmentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get invoice by appointment ID")
  public ResponseEntity<PaymentDto> getPaymentByAppointment(
      @PathVariable final String appointmentId) {
    return ResponseEntity.ok(paymentApi.getByAppointment(appointmentId));
  }

  @PostMapping
  @PreAuthorize("hasRole('STAFF')")
  @Operation(summary = "Create invoice for appointment")
  public ResponseEntity<PaymentDto> createInvoice(@RequestBody final PaymentDto paymentDto) {
    return ResponseEntity.ok(paymentApi.createInvoice(paymentDto));
  }

  @PostMapping("/{id}/pay")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Process invoice payment")
  public ResponseEntity<PaymentDto> processPayment(
      @PathVariable final String id, @RequestParam final String method) {
    return ResponseEntity.ok(paymentApi.processPayment(id, method));
  }
}
