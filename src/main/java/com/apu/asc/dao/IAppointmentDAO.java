package com.apu.asc.dao;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public interface IAppointmentDAO {
  public void reload();
  public void save(Appointment appointment);
  public Appointment findById(String id);
  public List<Appointment> findByCustomer(String customerId);
  public List<Appointment> findByTechnician(String technicianId);
  public List<Appointment> findByStatus(ApptStatus status);
  public List<Appointment> getAll();
}
