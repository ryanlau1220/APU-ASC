package com.apu.asc.payment.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
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
  @Mock private CurrentUserService currentUserService;

  private PaymentController controller;

  @BeforeEach
  void setUp() {
    controller =
        new PaymentController(paymentApi, workOrderApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotProcessAnotherCustomersInvoice() {
    when(paymentApi.getPaymentById("PAY-OTHER")).thenReturn(payment("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.processPayment("PAY-OTHER", "ONLINE_CARD", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
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
}
