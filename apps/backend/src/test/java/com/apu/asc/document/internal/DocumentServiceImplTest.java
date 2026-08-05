package com.apu.asc.document.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.storage.ObjectStorageApi;
import com.apu.asc.document.DocumentDto;
import com.apu.asc.document.DocumentType;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  @Mock private DocumentRepository documentRepository;
  @Mock private ObjectStorageApi objectStorage;
  @Mock private WorkOrderApi workOrderApi;
  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  void storesServerGeneratedKeyAndCanonicalFileMetadata() {
    when(documentRepository.save(any(DocumentEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(workOrderApi.getWorkOrderById("WO-1")).thenReturn(workOrder());
    DocumentServiceImpl service = service();
    MockMultipartFile file =
        new MockMultipartFile("file", "../../before repair.png", "text/plain", pngContent());

    DocumentDto uploaded = service.upload("WO-1", DocumentType.REPAIR_EVIDENCE, file, "USR-TECH");

    assertThat(uploaded.fileName()).isEqualTo("before repair.png");
    assertThat(uploaded.contentType()).isEqualTo("image/png");
    ArgumentCaptor<String> storageKey = ArgumentCaptor.forClass(String.class);
    verify(objectStorage)
        .put(storageKey.capture(), any(), org.mockito.ArgumentMatchers.eq("image/png"));
    assertThat(storageKey.getValue()).startsWith("work-orders/WO-1/documents/DOC-");
    assertThat(storageKey.getValue()).endsWith(".png");
  }

  @Test
  void rejectsUnsupportedContentBeforeStorage() {
    DocumentServiceImpl service = service();
    MockMultipartFile file =
        new MockMultipartFile("file", "unsafe.svg", "image/svg+xml", "<svg/>".getBytes());

    assertThatThrownBy(() -> service.upload("WO-1", DocumentType.OTHER, file, "USR-TECH"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Only JPEG, PNG, WebP, and PDF documents are allowed.");

    verify(objectStorage, never()).put(any(), any(), any());
  }

  private DocumentServiceImpl service() {
    return new DocumentServiceImpl(documentRepository, objectStorage, workOrderApi, eventPublisher);
  }

  private WorkOrderDto workOrder() {
    return new WorkOrderDto(
        "WO-1",
        null,
        "USR-CUSTOMER",
        "VEH-1",
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

  private byte[] pngContent() {
    return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
  }
}
