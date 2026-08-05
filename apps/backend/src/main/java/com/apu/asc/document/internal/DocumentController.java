package com.apu.asc.document.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.document.DocumentApi;
import com.apu.asc.document.DocumentDto;
import com.apu.asc.document.DocumentType;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Secure work-order evidence and supporting files")
class DocumentController {

  private final DocumentApi documentApi;
  private final WorkOrderApi workOrderApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping("/work-orders/{workOrderId}/documents")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(operationId = "getWorkOrderDocuments", summary = "List documents for a work order")
  ResponseEntity<List<DocumentDto>> findByWorkOrder(
      @PathVariable String workOrderId, Authentication authentication) {
    requireWorkOrderRead(workOrderId, authentication);
    return ResponseEntity.ok(documentApi.findByWorkOrderId(workOrderId));
  }

  @PostMapping(
      value = "/work-orders/{workOrderId}/documents",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      operationId = "uploadWorkOrderDocument",
      summary = "Upload evidence or a supporting file")
  ResponseEntity<DocumentDto> upload(
      @PathVariable String workOrderId,
      @RequestParam("type") DocumentType type,
      @RequestParam("file") MultipartFile file,
      Authentication authentication) {
    AuthenticatedUser currentUser = requireWorkOrderRead(workOrderId, authentication);
    return ResponseEntity.ok(documentApi.upload(workOrderId, type, file, currentUser.id()));
  }

  @GetMapping("/documents/{id}/download")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(operationId = "downloadDocument", summary = "Download a document")
  ResponseEntity<InputStreamResource> download(
      @PathVariable String id, Authentication authentication) {
    DocumentDto document = documentApi.getById(id);
    requireWorkOrderRead(document.workOrderId(), authentication);
    InputStream content = documentApi.openContent(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(document.contentType()))
        .contentLength(document.sizeBytes())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(document.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(new InputStreamResource(content));
  }

  private AuthenticatedUser requireWorkOrderRead(
      String workOrderId, Authentication authentication) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(workOrderId);
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    accessPolicy.requireWorkOrderRead(
        currentUser, workOrder.customerId(), workOrder.technicianId());
    return currentUser;
  }
}
