package com.apu.asc.document.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.common.storage.ObjectStorageApi;
import com.apu.asc.document.DocumentApi;
import com.apu.asc.document.DocumentDto;
import com.apu.asc.document.DocumentType;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
class DocumentServiceImpl implements DocumentApi {

  private static final int MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

  private final DocumentRepository documentRepository;
  private final ObjectStorageApi objectStorage;
  private final WorkOrderApi workOrderApi;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<DocumentDto> findByWorkOrderId(String workOrderId) {
    return documentRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentDto getById(String id) {
    return toDto(findEntity(id));
  }

  @Override
  @Transactional
  public DocumentDto upload(
      String workOrderId, DocumentType type, MultipartFile file, String uploadedByUserId) {
    byte[] content = readAndValidate(file);
    String contentType = detectContentType(content);
    String id = "DOC-" + UUID.randomUUID();
    String storageKey =
        "work-orders/" + workOrderId + "/documents/" + id + extensionFor(contentType);
    DocumentEntity entity =
        DocumentEntity.builder()
            .id(id)
            .workOrderId(workOrderId)
            .uploadedBy(uploadedByUserId)
            .type(type)
            .fileName(normalizeFileName(file.getOriginalFilename(), contentType))
            .storageKey(storageKey)
            .contentType(contentType)
            .sizeBytes(content.length)
            .build();

    objectStorage.put(storageKey, content, contentType);
    try {
      DocumentDto uploaded = toDto(documentRepository.save(entity));
      publishUpload(uploaded);
      return uploaded;
    } catch (RuntimeException exception) {
      objectStorage.delete(storageKey);
      throw exception;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public InputStream openContent(String id) {
    return objectStorage.get(findEntity(id).getStorageKey());
  }

  private byte[] readAndValidate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("A non-empty document file is required.");
    }
    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new IllegalArgumentException("Documents cannot exceed 10 MB.");
    }
    try {
      byte[] content = file.getBytes();
      if (content.length == 0 || content.length > MAX_FILE_SIZE_BYTES) {
        throw new IllegalArgumentException("Documents cannot exceed 10 MB.");
      }
      return content;
    } catch (IOException exception) {
      throw new IllegalArgumentException("The uploaded document could not be read.");
    }
  }

  private String detectContentType(byte[] content) {
    if (startsWith(content, 0xFF, 0xD8, 0xFF)) return "image/jpeg";
    if (startsWith(content, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) return "image/png";
    if (content.length >= 12
        && startsWith(content, 0x52, 0x49, 0x46, 0x46)
        && content[8] == 'W'
        && content[9] == 'E'
        && content[10] == 'B'
        && content[11] == 'P') return "image/webp";
    if (startsWith(content, 0x25, 0x50, 0x44, 0x46, 0x2D)) return "application/pdf";
    throw new IllegalArgumentException("Only JPEG, PNG, WebP, and PDF documents are allowed.");
  }

  private boolean startsWith(byte[] content, int... prefix) {
    if (content.length < prefix.length) return false;
    for (int index = 0; index < prefix.length; index++) {
      if ((content[index] & 0xFF) != prefix[index]) return false;
    }
    return true;
  }

  private String normalizeFileName(String originalFileName, String contentType) {
    String raw = originalFileName == null ? "attachment" : originalFileName.replace('\\', '/');
    String base =
        raw.substring(raw.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
    int extensionIndex = base.lastIndexOf('.');
    if (extensionIndex > 0) base = base.substring(0, extensionIndex);
    if (base.isBlank()) base = "attachment";
    base = base.substring(0, Math.min(base.length(), 120));
    return base + extensionFor(contentType);
  }

  private String extensionFor(String contentType) {
    return switch (contentType) {
      case "image/jpeg" -> ".jpg";
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      case "application/pdf" -> ".pdf";
      default -> throw new IllegalArgumentException("Unsupported document content type.");
    };
  }

  private DocumentEntity findEntity(String id) {
    return documentRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Document", id));
  }

  private void publishUpload(DocumentDto document) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(document.workOrderId());
    eventPublisher.publishEvent(
        new AuditEvent(
            document.uploadedBy(),
            "DOCUMENT_UPLOADED",
            "DOCUMENT",
            document.id(),
            "Uploaded " + document.type() + " for work order " + document.workOrderId()));
    eventPublisher.publishEvent(
        LiveUpdateEvent.forUsersAndRoles(
            "documents",
            document.workOrderId(),
            Arrays.asList(workOrder.customerId(), workOrder.technicianId()),
            Set.of("STAFF", "MANAGER")));
  }

  private DocumentDto toDto(DocumentEntity entity) {
    return new DocumentDto(
        entity.getId(),
        entity.getWorkOrderId(),
        entity.getType(),
        entity.getFileName(),
        entity.getContentType(),
        entity.getSizeBytes(),
        entity.getUploadedBy(),
        entity.getCreatedAt());
  }
}
