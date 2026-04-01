package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.AuditLogDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface IBackupService {
  public Path backup() throws IOException;
  public void restore(Path zipPath) throws IOException;
}
