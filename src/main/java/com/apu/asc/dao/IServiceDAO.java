package com.apu.asc.dao;

import com.apu.asc.model.Service;
import java.util.List;

public interface IServiceDAO {
  public void reload();
  public void save(Service service);
  public Service findById(String id);
  public List<Service> getActiveServices();
  public List<Service> getAll();
}
