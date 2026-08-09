package com.apu.asc.payment.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.payment.CheckoutSessionDto;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PaymentControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private PaymentApi paymentApi;
  @Mock private WorkOrderApi workOrderApi;
  @Mock private QuotationApi quotationApi;
  @Mock private CurrentUserService currentUserService;
  @Mock private StripeCheckoutService stripeCheckoutService;

  private PaymentController controller;

  @BeforeEach
  void setUp() {
    controller =
        new PaymentController(
            paymentApi,
            workOrderApi,
            quotationApi,
            currentUserService,
            new AccessPolicy(),
            stripeCheckoutService);
  }

  @Test
  void customerCannotStartCheckoutForAnotherCustomersInvoice() {
    when(paymentApi.getPaymentById("PAY-OTHER")).thenReturn(payment("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.createCheckoutSession("PAY-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void customerCanStartCheckoutForOwnInvoice() {
    when(paymentApi.getPaymentById("PAY-MINE")).thenReturn(payment("USR-CUSTOMER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));
    when(stripeCheckoutService.createCheckoutSession("PAY-MINE"))
        .thenReturn(new CheckoutSessionDto("https://checkout.stripe.com/c/pay/cs_test"));

    controller.createCheckoutSession("PAY-MINE", AUTHENTICATION);

    verify(stripeCheckoutService).createCheckoutSession("PAY-MINE");
  }

  @Test
  void invoiceUsesTheApprovedQuotationTotalInsteadOfClientAmount() {
    WorkOrderDto workOrder =
        new WorkOrderDto(
            "WO-1",
            "APT-1",
            "USR-1",
            "VEH-1",
            "SVC-1",
            null,
            "IN_PROGRESS",
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    PaymentDto request =
        new PaymentDto(
            null, null, null, null, new BigDecimal("1.00"), null, null, null, null, "WO-1");
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-STAFF", Set.of("STAFF")));
    when(workOrderApi.getWorkOrderById("WO-1")).thenReturn(workOrder);
    when(quotationApi.getApprovedByWorkOrder("WO-1"))
        .thenReturn(quotation(new BigDecimal("350.00")));
    when(paymentApi.createInvoice(any())).thenAnswer(invocation -> invocation.getArgument(0));

    controller.createInvoice(request, AUTHENTICATION);

    org.mockito.ArgumentCaptor<PaymentDto> captured =
        org.mockito.ArgumentCaptor.forClass(PaymentDto.class);
    verify(paymentApi).createInvoice(captured.capture());
    org.assertj.core.api.Assertions.assertThat(captured.getValue().amount())
        .isEqualByComparingTo("350.00");
  }

  private PaymentDto payment(String customerId) {
    return new PaymentDto(
        "PAY-OTHER",
        "APT-OTHER",
        customerId,
        "INV-OTHER",
        BigDecimal.TEN,
        "ONLINE_CARD",
        "UNPAID",
        null,
        null,
        null);
  }

  private QuotationDto quotation(BigDecimal totalAmount) {
    return new QuotationDto(
        "QTE-1",
        "QT-1",
        "WO-1",
        "USR-1",
        1,
        "APPROVED",
        null,
        null,
        java.time.LocalDate.now().plusDays(1),
        totalAmount,
        BigDecimal.ZERO,
        totalAmount,
        null,
        null,
        null,
        null,
        java.util.List.of());
  }
}
