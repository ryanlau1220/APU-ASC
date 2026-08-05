package com.apu.asc.common.storage;

import java.io.InputStream;

/** Private object storage operations. Callers must generate and retain their own object keys. */
public interface ObjectStorageApi {
  void put(String key, byte[] content, String contentType);

  InputStream get(String key);

  void delete(String key);
}
