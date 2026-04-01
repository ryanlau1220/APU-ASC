package com.apu.asc.dao;

import com.apu.asc.model.AuditLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface IAuditLogDAO {
  public void reload();
  public void append(AuditLog log);
  public List<AuditLog> getAll();
}
