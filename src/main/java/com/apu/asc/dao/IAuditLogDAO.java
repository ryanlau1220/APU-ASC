package com.apu.asc.dao;

import com.apu.asc.model.AuditLog;
import java.util.List;

public interface IAuditLogDAO {
  public void reload();
  public void append(AuditLog log);
  public List<AuditLog> getAll();
}
