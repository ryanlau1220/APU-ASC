package com.apu.asc.document.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.document.DocumentApi;
import com.apu.asc.document.DocumentDto;
import com.apu.asc.document.DocumentType;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import java.time.Instant;
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
class DocumentControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private DocumentApi documentApi;
  @Mock private WorkOrderApi workOrderApi;
  @Mock private CurrentUserService currentUserService;

  private DocumentController controller;

  @BeforeEach
  void setUp() {
    controller =
        new DocumentController(documentApi, workOrderApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotDownloadAnotherCustomersDocument() {
    when(documentApi.getById("DOC-OTHER")).thenReturn(document());
    when(workOrderApi.getWorkOrderById("WO-OTHER")).thenReturn(workOrder());
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.download("DOC-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);

    verify(documentApi, never()).openContent("DOC-OTHER");
  }

  private DocumentDto document() {
    return new DocumentDto(
        "DOC-OTHER",
        "WO-OTHER",
        DocumentType.REPAIR_EVIDENCE,
        "evidence.jpg",
        "image/jpeg",
        100,
        "USR-TECH",
        Instant.now());
  }

  private WorkOrderDto workOrder() {
    return new WorkOrderDto(
        "WO-OTHER",
        null,
        "USR-OTHER",
        "VEH-OTHER",
        "SVC-1",
        "USR-TECH",
        "OPEN",
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }
}
