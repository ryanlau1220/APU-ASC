package com.apu.asc.document;

import java.io.InputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentApi {
  List<DocumentDto> findByWorkOrderId(String workOrderId);

  DocumentDto getById(String id);

  DocumentDto upload(
      String workOrderId, DocumentType type, MultipartFile file, String uploadedByUserId);

  InputStream openContent(String id);
}
