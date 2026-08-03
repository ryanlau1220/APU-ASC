package com.apu.asc.quotation.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDecisionRequestDto;
import com.apu.asc.quotation.QuotationDraftRequestDto;
import com.apu.asc.quotation.QuotationDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotations", description = "Workshop estimates and customer approval APIs")
class QuotationController {

  private final QuotationApi quotationApi;
  private final WorkOrderApi workOrderApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(operationId = "getAllQuotations", summary = "Get all quotations")
  public ResponseEntity<List<QuotationDto>> getAllQuotations() {
    return ResponseEntity.ok(quotationApi.findAllQuotations());
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(
      operationId = "getMyQuotations",
      summary = "Get quotations for the authenticated customer")
  public ResponseEntity<List<QuotationDto>> getMyQuotations(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(quotationApi.findByCustomer(currentUser.id()));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(operationId = "getQuotationById", summary = "Get quotation by ID")
  public ResponseEntity<QuotationDto> getQuotationById(
      @PathVariable String id, Authentication authentication) {
    QuotationDto quotation = quotationApi.getQuotationById(id);
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(quotation.workOrderId());
    accessPolicy.requireWorkOrderRead(
        currentUserService.requireCurrentUser(authentication),
        quotation.customerId(),
        workOrder.technicianId());
    return ResponseEntity.ok(quotation);
  }

  @GetMapping("/work-order/{workOrderId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      operationId = "getQuotationsByWorkOrder",
      summary = "Get quotation revision history for a work order")
  public ResponseEntity<List<QuotationDto>> getByWorkOrder(
      @PathVariable String workOrderId, Authentication authentication) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(workOrderId);
    accessPolicy.requireWorkOrderRead(
        currentUserService.requireCurrentUser(authentication),
        workOrder.customerId(),
        workOrder.technicianId());
    return ResponseEntity.ok(quotationApi.findByWorkOrder(workOrderId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(
      operationId = "createQuotationDraft",
      summary = "Create a quotation draft for an open work order")
  public ResponseEntity<QuotationDto> createDraft(
      @Valid @RequestBody QuotationDraftRequestDto request, Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(request.workOrderId());
    if ("CANCELLED".equals(workOrder.status()) || "COMPLETED".equals(workOrder.status())) {
      throw new IllegalArgumentException("A quotation cannot be created for a closed work order.");
    }
    QuotationDto created = quotationApi.createDraft(workOrder.customerId(), request);
    return ResponseEntity.created(URI.create("/api/v1/quotations/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(operationId = "updateQuotationDraft", summary = "Update a quotation draft")
  public ResponseEntity<QuotationDto> updateDraft(
      @PathVariable String id,
      @Valid @RequestBody QuotationDraftRequestDto request,
      Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(quotationApi.updateDraft(id, request));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(operationId = "submitQuotation", summary = "Submit a quotation for customer approval")
  public ResponseEntity<QuotationDto> submit(
      @PathVariable String id, Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    return ResponseEntity.ok(quotationApi.submit(id));
  }

  @PostMapping("/{id}/decision")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(operationId = "decideQuotation", summary = "Approve or reject a pending quotation")
  public ResponseEntity<QuotationDto> decide(
      @PathVariable String id,
      @Valid @RequestBody QuotationDecisionRequestDto request,
      Authentication authentication) {
    QuotationDto quotation = quotationApi.getQuotationById(id);
    accessPolicy.requireSelf(
        currentUserService.requireCurrentUser(authentication), quotation.customerId());
    return ResponseEntity.ok(quotationApi.decide(id, request));
  }
}
