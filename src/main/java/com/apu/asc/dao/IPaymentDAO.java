package com.apu.asc.dao;

import com.apu.asc.model.Payment;
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

public interface IPaymentDAO {
  public void reload();
  public void save(Payment payment);
  public Payment findByAppointment(String appointmentId);
  public List<Payment> getAll();
}
