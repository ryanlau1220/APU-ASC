package com.apu.asc.quotation.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDecisionRequestDto;
import com.apu.asc.quotation.QuotationDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
class QuotationControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private QuotationApi quotationApi;
  @Mock private WorkOrderApi workOrderApi;
  @Mock private CurrentUserService currentUserService;

  private QuotationController controller;

  @BeforeEach
  void setUp() {
    controller =
        new QuotationController(quotationApi, workOrderApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotDecideAnotherCustomersQuotation() {
    when(quotationApi.getQuotationById("QTE-OTHER")).thenReturn(quotation("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(
            () ->
                controller.decide(
                    "QTE-OTHER",
                    new QuotationDecisionRequestDto(
                        QuotationDecisionRequestDto.Decision.APPROVE, null),
                    AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  private QuotationDto quotation(String customerId) {
    return new QuotationDto(
        "QTE-OTHER",
        "QT-OTHER",
        "WO-OTHER",
        customerId,
        1,
        "PENDING_APPROVAL",
        null,
        null,
        LocalDate.now().plusDays(1),
        BigDecimal.TEN,
        BigDecimal.ZERO,
        BigDecimal.TEN,
        null,
        null,
        null,
        null,
        List.of());
  }
}
