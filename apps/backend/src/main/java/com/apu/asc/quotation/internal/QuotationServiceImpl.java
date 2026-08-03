package com.apu.asc.quotation.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.common.event.QuotationApprovedEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.quotation.QuotationApi;
import com.apu.asc.quotation.QuotationDecisionRequestDto;
import com.apu.asc.quotation.QuotationDraftRequestDto;
import com.apu.asc.quotation.QuotationDto;
import com.apu.asc.quotation.QuotationExpiredException;
import com.apu.asc.quotation.QuotationLineDto;
import com.apu.asc.quotation.QuotationLineRequestDto;
import com.apu.asc.quotation.QuotationStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class QuotationServiceImpl implements QuotationApi {

  private static final BigDecimal ZERO = new BigDecimal("0.00");

  private final QuotationRepository quotationRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<QuotationDto> findAllQuotations() {
    return quotationRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<QuotationDto> findByCustomer(String customerId) {
    return quotationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<QuotationDto> findByWorkOrder(String workOrderId) {
    return quotationRepository.findByWorkOrderIdOrderByRevisionDesc(workOrderId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public QuotationDto getQuotationById(String id) {
    return toDto(findEntity(id));
  }

  @Override
  @Transactional
  public QuotationDto createDraft(String customerId, QuotationDraftRequestDto request) {
    requireDraftableWorkOrder(request.workOrderId());
    PriceSummary summary = price(request.items());
    QuotationEntity entity =
        QuotationEntity.builder()
            .id("QTE-" + UUID.randomUUID())
            .quoteNumber("QT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .workOrderId(request.workOrderId())
            .customerId(customerId)
            .revision(quotationRepository.countByWorkOrderId(request.workOrderId()) + 1)
            .status(QuotationStatus.DRAFT.name())
            .notes(request.notes())
            .validUntil(requireValidUntil(request.validUntil()))
            .subtotal(summary.subtotal())
            .taxAmount(ZERO)
            .totalAmount(summary.subtotal())
            .build();
    entity.replaceItems(summary.items());
    QuotationDto created = toDto(quotationRepository.save(entity));
    publish(created, "QUOTATION_DRAFTED", "Created quotation draft " + created.quoteNumber());
    return created;
  }

  @Override
  @Transactional
  public QuotationDto updateDraft(String id, QuotationDraftRequestDto request) {
    QuotationEntity entity = findEntity(id);
    requireDraft(entity);
    if (!entity.getWorkOrderId().equals(request.workOrderId())) {
      throw new IllegalArgumentException(
          "A quotation draft cannot be moved to another work order.");
    }
    PriceSummary summary = price(request.items());
    entity.setNotes(request.notes());
    entity.setValidUntil(requireValidUntil(request.validUntil()));
    entity.setSubtotal(summary.subtotal());
    entity.setTaxAmount(ZERO);
    entity.setTotalAmount(summary.subtotal());
    entity.replaceItems(summary.items());
    QuotationDto updated = toDto(quotationRepository.save(entity));
    publish(updated, "QUOTATION_DRAFT_UPDATED", "Updated quotation draft " + updated.quoteNumber());
    return updated;
  }

  @Override
  @Transactional
  public QuotationDto submit(String id) {
    QuotationEntity entity = findEntity(id);
    requireDraft(entity);
    entity.setStatus(QuotationStatus.PENDING_APPROVAL.name());
    entity.setSubmittedAt(Instant.now());
    QuotationDto submitted = toDto(quotationRepository.save(entity));
    publish(
        submitted,
        "QUOTATION_SUBMITTED",
        "Sent quotation " + submitted.quoteNumber() + " for customer approval");
    return submitted;
  }

  @Override
  @Transactional(noRollbackFor = QuotationExpiredException.class)
  public QuotationDto decide(String id, QuotationDecisionRequestDto request) {
    QuotationEntity entity = findEntity(id);
    if (QuotationStatus.fromString(entity.getStatus()) != QuotationStatus.PENDING_APPROVAL) {
      throw new IllegalArgumentException("Only a pending quotation can be approved or rejected.");
    }
    if (entity.getValidUntil().isBefore(LocalDate.now())) {
      entity.setStatus(QuotationStatus.EXPIRED.name());
      quotationRepository.save(entity);
      throw new QuotationExpiredException();
    }

    Instant respondedAt = Instant.now();
    boolean approved = request.decision() == QuotationDecisionRequestDto.Decision.APPROVE;
    String beforeState = quotationAuditState(entity);
    entity.setStatus(approved ? QuotationStatus.APPROVED.name() : QuotationStatus.REJECTED.name());
    entity.setResponseNotes(request.responseNotes());
    entity.setRespondedAt(respondedAt);
    QuotationDto decided = toDto(quotationRepository.save(entity));
    publish(
        decided,
        approved ? "QUOTATION_APPROVED" : "QUOTATION_REJECTED",
        (approved ? "Approved " : "Rejected ") + decided.quoteNumber(),
        beforeState,
        quotationAuditState(entity));
    if (approved) {
      eventPublisher.publishEvent(
          new QuotationApprovedEvent(decided.workOrderId(), decided.id(), respondedAt));
    }
    return decided;
  }

  @Override
  @Transactional(readOnly = true)
  public QuotationDto getApprovedByWorkOrder(String workOrderId) {
    return quotationRepository
        .findFirstByWorkOrderIdAndStatusOrderByRevisionDesc(
            workOrderId, QuotationStatus.APPROVED.name())
        .map(this::toDto)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "An approved quotation is required before invoicing this work order."));
  }

  private void requireDraftableWorkOrder(String workOrderId) {
    boolean outstanding =
        quotationRepository.findByWorkOrderIdOrderByRevisionDesc(workOrderId).stream()
            .map(QuotationEntity::getStatus)
            .map(QuotationStatus::fromString)
            .anyMatch(
                status ->
                    status == QuotationStatus.DRAFT || status == QuotationStatus.PENDING_APPROVAL);
    if (outstanding) {
      throw new IllegalArgumentException(
          "This work order already has a draft or pending quotation. Update or resolve it first.");
    }
  }

  private void requireDraft(QuotationEntity entity) {
    if (QuotationStatus.fromString(entity.getStatus()) != QuotationStatus.DRAFT) {
      throw new IllegalArgumentException("Only a draft quotation can be changed or submitted.");
    }
  }

  private LocalDate requireValidUntil(LocalDate validUntil) {
    if (validUntil == null || validUntil.isBefore(LocalDate.now())) {
      throw new IllegalArgumentException(
          "A quotation validity date of today or later is required.");
    }
    return validUntil;
  }

  private PriceSummary price(List<QuotationLineRequestDto> requests) {
    if (requests == null || requests.isEmpty()) {
      throw new IllegalArgumentException("A quotation must contain at least one line item.");
    }
    List<QuotationLineEntity> items =
        requests.stream()
            .map(
                request -> {
                  BigDecimal quantity = scale(request.quantity(), "Quantity");
                  BigDecimal unitPrice = scale(request.unitPrice(), "Unit price");
                  if (quantity.signum() <= 0 || unitPrice.signum() < 0) {
                    throw new IllegalArgumentException(
                        "Quotation quantities and prices must be valid.");
                  }
                  return QuotationLineEntity.builder()
                      .id("QTI-" + UUID.randomUUID())
                      .description(request.description().trim())
                      .quantity(quantity)
                      .unitPrice(unitPrice)
                      .lineTotal(quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP))
                      .build();
                })
            .toList();
    BigDecimal subtotal =
        items.stream()
            .map(QuotationLineEntity::getLineTotal)
            .reduce(ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    return new PriceSummary(items, subtotal);
  }

  private BigDecimal scale(BigDecimal value, String field) {
    if (value == null) {
      throw new IllegalArgumentException(field + " is required.");
    }
    try {
      return value.setScale(2, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(field + " must have no more than two decimal places.");
    }
  }

  private QuotationEntity findEntity(String id) {
    return quotationRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
  }

  private void publish(QuotationDto quotation, String action, String details) {
    publish(quotation, action, details, null, null);
  }

  private void publish(
      QuotationDto quotation,
      String action,
      String details,
      String beforeState,
      String afterState) {
    eventPublisher.publishEvent(
        LiveUpdateEvent.forUsersAndRoles(
            "quotations",
            quotation.id(),
            Set.of(quotation.customerId()),
            Set.of("STAFF", "MANAGER")));
    eventPublisher.publishEvent(
        new AuditEvent(
            quotation.customerId(),
            action,
            "QUOTATION",
            quotation.id(),
            details,
            beforeState,
            afterState));
  }

  private String quotationAuditState(QuotationEntity entity) {
    return "status="
        + entity.getStatus()
        + "; totalAmount="
        + entity.getTotalAmount()
        + "; revision="
        + entity.getRevision();
  }

  private QuotationDto toDto(QuotationEntity entity) {
    return new QuotationDto(
        entity.getId(),
        entity.getQuoteNumber(),
        entity.getWorkOrderId(),
        entity.getCustomerId(),
        entity.getRevision(),
        entity.getStatus(),
        entity.getNotes(),
        entity.getResponseNotes(),
        entity.getValidUntil(),
        entity.getSubtotal(),
        entity.getTaxAmount(),
        entity.getTotalAmount(),
        entity.getSubmittedAt(),
        entity.getRespondedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getItems().stream()
            .map(
                item ->
                    new QuotationLineDto(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()))
            .toList());
  }

  private record PriceSummary(List<QuotationLineEntity> items, BigDecimal subtotal) {}
}
