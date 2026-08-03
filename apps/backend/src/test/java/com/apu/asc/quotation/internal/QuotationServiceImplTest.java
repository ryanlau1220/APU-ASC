package com.apu.asc.quotation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.event.QuotationApprovedEvent;
import com.apu.asc.quotation.QuotationDecisionRequestDto;
import com.apu.asc.quotation.QuotationDraftRequestDto;
import com.apu.asc.quotation.QuotationExpiredException;
import com.apu.asc.quotation.QuotationLineRequestDto;
import com.apu.asc.quotation.QuotationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class QuotationServiceImplTest {

  @Mock private QuotationRepository quotationRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private QuotationServiceImpl quotationService;

  @BeforeEach
  void setUp() {
    quotationService = new QuotationServiceImpl(quotationRepository, eventPublisher);
  }

  @Test
  void calculatesTotalsOnTheServerWhenCreatingADraft() {
    when(quotationRepository.findByWorkOrderIdOrderByRevisionDesc("WO-1")).thenReturn(List.of());
    when(quotationRepository.countByWorkOrderId("WO-1")).thenReturn(0);
    when(quotationRepository.save(any()))
        .thenAnswer(
            invocation -> {
              QuotationEntity entity = invocation.getArgument(0);
              entity.prePersist();
              return entity;
            });

    var quotation = quotationService.createDraft("USR-1", draft("WO-1"));

    assertThat(quotation.status()).isEqualTo(QuotationStatus.DRAFT.name());
    assertThat(quotation.subtotal()).isEqualByComparingTo("250.00");
    assertThat(quotation.totalAmount()).isEqualByComparingTo("250.00");
    assertThat(quotation.items()).hasSize(2);
  }

  @Test
  void preventsASecondOutstandingQuotationForTheSameWorkOrder() {
    QuotationEntity outstanding =
        QuotationEntity.builder()
            .id("QTE-1")
            .status(QuotationStatus.PENDING_APPROVAL.name())
            .build();
    when(quotationRepository.findByWorkOrderIdOrderByRevisionDesc("WO-1"))
        .thenReturn(List.of(outstanding));

    assertThatThrownBy(() -> quotationService.createDraft("USR-1", draft("WO-1")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pending quotation");
  }

  @Test
  void approvingAPendingQuotationPublishesTheWorkOrderApprovalEvent() {
    QuotationEntity entity =
        QuotationEntity.builder()
            .id("QTE-1")
            .quoteNumber("QT-1")
            .workOrderId("WO-1")
            .customerId("USR-1")
            .status(QuotationStatus.PENDING_APPROVAL.name())
            .validUntil(LocalDate.now().plusDays(7))
            .subtotal(new BigDecimal("100.00"))
            .taxAmount(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("100.00"))
            .build();
    when(quotationRepository.findById("QTE-1")).thenReturn(Optional.of(entity));
    when(quotationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var decided =
        quotationService.decide(
            "QTE-1",
            new QuotationDecisionRequestDto(QuotationDecisionRequestDto.Decision.APPROVE, null));

    ArgumentCaptor<QuotationApprovedEvent> event =
        ArgumentCaptor.forClass(QuotationApprovedEvent.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertThat(decided.status()).isEqualTo(QuotationStatus.APPROVED.name());
    assertThat(event.getValue().workOrderId()).isEqualTo("WO-1");
    assertThat(event.getValue().quotationId()).isEqualTo("QTE-1");
  }

  @Test
  void rejectsAmountsWithMoreThanTwoDecimalPlaces() {
    when(quotationRepository.findByWorkOrderIdOrderByRevisionDesc("WO-1")).thenReturn(List.of());

    QuotationDraftRequestDto invalid =
        new QuotationDraftRequestDto(
            "WO-1",
            null,
            LocalDate.now().plusDays(7),
            List.of(new QuotationLineRequestDto("Oil", new BigDecimal("1.001"), BigDecimal.TEN)));

    assertThatThrownBy(() -> quotationService.createDraft("USR-1", invalid))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("two decimal places");
  }

  @Test
  void marksAnExpiredQuotationBeforeRejectingItsApprovalAttempt() {
    QuotationEntity entity =
        QuotationEntity.builder()
            .id("QTE-1")
            .status(QuotationStatus.PENDING_APPROVAL.name())
            .validUntil(LocalDate.now().minusDays(1))
            .build();
    when(quotationRepository.findById("QTE-1")).thenReturn(Optional.of(entity));
    when(quotationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    assertThatThrownBy(
            () ->
                quotationService.decide(
                    "QTE-1",
                    new QuotationDecisionRequestDto(
                        QuotationDecisionRequestDto.Decision.APPROVE, null)))
        .isInstanceOf(QuotationExpiredException.class);

    assertThat(entity.getStatus()).isEqualTo(QuotationStatus.EXPIRED.name());
    verify(quotationRepository).save(entity);
  }

  private QuotationDraftRequestDto draft(String workOrderId) {
    return new QuotationDraftRequestDto(
        workOrderId,
        "Replace worn parts",
        LocalDate.now().plusDays(7),
        List.of(
            new QuotationLineRequestDto(
                "Brake pads", new BigDecimal("2.00"), new BigDecimal("100.00")),
            new QuotationLineRequestDto("Labour", BigDecimal.ONE, new BigDecimal("50.00"))));
  }
}
