package com.apu.asc.payment.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.payment.CheckoutSessionDto;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.payment.PaymentExceptionRequest;
import com.apu.asc.payment.PaymentMethod;
import com.apu.asc.payment.PaymentRecordDto;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import com.apu.asc.workorder.WorkOrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments & Invoicing", description = "Invoice generation and payment processing APIs")
class PaymentController {

  private final PaymentApi paymentApi;
  private final WorkOrderApi workOrderApi;
  private final QuotationApi quotationApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;
  private final StripeCheckoutService stripeCheckoutService;

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
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Create invoice for appointment")
  public ResponseEntity<PaymentDto> createInvoice(
      @Valid @RequestBody final PaymentDto paymentDto, Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    WorkOrderDto workOrder = resolveWorkOrder(paymentDto);
    if (!WorkOrderStatus.COMPLETED.name().equals(workOrder.status())) {
      throw new IllegalArgumentException(
          "An invoice can only be issued for a completed work order.");
    }
    QuotationDto approvedQuotation = quotationApi.getApprovedByWorkOrder(workOrder.id());
    PaymentDto securedPayment =
        new PaymentDto(
            null,
            workOrder.appointmentId(),
            workOrder.customerId(),
            paymentDto.invoiceNumber(),
            approvedQuotation.totalAmount(),
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
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Record a counter cash or card payment")
  public ResponseEntity<PaymentDto> processPayment(
      @PathVariable final String id,
      @RequestParam final String method,
      Authentication authentication) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(paymentApi.processPayment(id, method));
  }

  @PostMapping("/{id}/checkout")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Create or resume Stripe Checkout for my invoice")
  public ResponseEntity<CheckoutSessionDto> createCheckoutSession(
      @PathVariable final String id, Authentication authentication) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(id));
  }

  @PostMapping("/{id}/void")
  @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
  @Operation(summary = "Void an unpaid invoice with a recorded reason")
  public ResponseEntity<PaymentDto> voidInvoice(
      @PathVariable final String id, @Valid @RequestBody final PaymentExceptionRequest request) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    if (PaymentMethod.STRIPE_CHECKOUT.name().equals(payment.paymentMethod())) {
      return ResponseEntity.ok(stripeCheckoutService.voidInvoice(id, request.reason()));
    }
    return ResponseEntity.ok(paymentApi.voidInvoice(id, request.reason()));
  }

  @PostMapping("/{id}/refund")
  @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
  @Operation(summary = "Issue a full refund with a recorded reason")
  public ResponseEntity<PaymentDto> refundPayment(
      @PathVariable final String id, @Valid @RequestBody final PaymentExceptionRequest request) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    if (PaymentMethod.STRIPE_CHECKOUT.name().equals(payment.paymentMethod())) {
      return ResponseEntity.ok(stripeCheckoutService.requestRefund(id, request.reason()));
    }
    return ResponseEntity.ok(paymentApi.refundCounterPayment(id, request.reason()));
  }

  @GetMapping(value = "/{id}/record", produces = MediaType.TEXT_HTML_VALUE)
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Open a printable payment receipt or credit note")
  public ResponseEntity<String> getPrintableRecord(
      @PathVariable final String id, Authentication authentication) {
    PaymentDto payment = paymentApi.getPaymentById(id);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), payment.customerId());
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'")
        .header("X-Content-Type-Options", "nosniff")
        .body(renderFinancialRecord(paymentApi.getPaymentRecord(id)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
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

  private String renderFinancialRecord(PaymentRecordDto record) {
    String heading =
        switch (record.paymentStatus()) {
          case "PAID" -> "Payment receipt";
          case "REFUND_PENDING" -> "Refund record";
          case "REFUNDED" -> "Credit note";
          case "VOID" -> "Void notice";
          default -> "Invoice record";
        };
    String eventLabel =
        record.refundedAt() != null ? "Refunded" : record.voidedAt() != null ? "Voided" : "Paid";
    String eventDate =
        record.refundedAt() != null
            ? formatDate(record.refundedAt())
            : record.voidedAt() != null
                ? formatDate(record.voidedAt())
                : record.paidAt() != null ? formatDate(record.paidAt()) : "Not settled";
    String exceptionDetail =
        record.refundReason() != null
            ? "<tr><th>Refund reason</th><td>%s</td></tr>".formatted(escape(record.refundReason()))
            : record.voidReason() != null
                ? "<tr><th>Void reason</th><td>%s</td></tr>".formatted(escape(record.voidReason()))
                : "";
    return """
        <!doctype html>
        <html lang="en"><head><meta charset="utf-8"><title>%s</title>
        <style>body{font-family:Arial,sans-serif;color:#152238;margin:48px;max-width:720px}h1{margin-bottom:4px}.muted{color:#60738f}table{border-collapse:collapse;width:100%%;margin-top:28px}th,td{border-bottom:1px solid #dbe4ef;padding:12px;text-align:left}th{width:38%%;color:#526783}.amount{font-size:24px;font-weight:700;color:#087fe7}@media print{body{margin:24px}}</style>
        </head><body><h1>%s</h1><p class="muted">APU Automotive Service Centre</p>
        <table><tr><th>Invoice</th><td>%s</td></tr><tr><th>Status</th><td>%s</td></tr><tr><th>Amount</th><td class="amount">RM %s</td></tr><tr><th>Method</th><td>%s</td></tr><tr><th>%s</th><td>%s</td></tr>%s</table>
        </body></html>
        """
        .formatted(
            escape(heading),
            escape(heading),
            escape(record.invoiceNumber()),
            escape(record.paymentStatus()),
            record.amount(),
            escape(record.paymentMethod()),
            eventLabel,
            eventDate,
            exceptionDetail);
  }

  private String formatDate(java.time.Instant value) {
    return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(value);
  }

  private String escape(String value) {
    return HtmlUtils.htmlEscape(value == null ? "—" : value);
  }
}
