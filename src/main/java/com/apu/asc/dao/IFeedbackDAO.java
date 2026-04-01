package com.apu.asc.dao;

import com.apu.asc.model.Feedback;
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

public interface IFeedbackDAO {
  public void reload();
  public void save(Feedback feedback);
  public Feedback findByAppointment(String appointmentId);
  public List<Feedback> findByTechnician(String technicianId, AppointmentDAO appointmentDAO);
  public List<Feedback> getAll();
}
