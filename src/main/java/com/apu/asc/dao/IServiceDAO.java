package com.apu.asc.dao;

import com.apu.asc.model.Service;
import com.apu.asc.model.ServiceType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public interface IServiceDAO {
  public void reload();
  public void save(Service service);
  public Service findById(String id);
  public List<Service> getActiveServices();
  public List<Service> getAll();
}
