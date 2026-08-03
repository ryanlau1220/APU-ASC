package com.apu.asc.feedback.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.feedback.FeedbackApi;
import com.apu.asc.feedback.FeedbackDto;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
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
class FeedbackControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private FeedbackApi feedbackApi;
  @Mock private WorkOrderApi workOrderApi;
  @Mock private CurrentUserService currentUserService;

  private FeedbackController controller;

  @BeforeEach
  void setUp() {
    controller =
        new FeedbackController(feedbackApi, workOrderApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotReadAnotherCustomersFeedback() {
    when(feedbackApi.getFeedbackById("FBK-OTHER")).thenReturn(feedback("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.getFeedbackById("FBK-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  private FeedbackDto feedback(String customerId) {
    return new FeedbackDto(
        "FBK-OTHER", "APT-OTHER", customerId, "USR-TECH", 5, "Great", null, null, "WO-OTHER");
  }
}
