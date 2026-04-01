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

public class BackupService implements IBackupService {

  private static final Path DATA_DIR = Path.of("data");
  private static final Path BACKUP_DIR = Path.of("backups");
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

  public Path backup() throws IOException {
    Files.createDirectories(BACKUP_DIR);
    Path zipPath = BACKUP_DIR.resolve("backup_" + LocalDateTime.now().format(FMT) + ".zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath));
        Stream<Path> walk = Files.walk(DATA_DIR)) {
      walk.filter(p -> p.toString().endsWith(".txt"))
          .forEach(
              p -> {
                try {
                  zos.putNextEntry(new ZipEntry(DATA_DIR.relativize(p).toString()));
                  Files.copy(p, zos);
                  zos.closeEntry();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
    return zipPath;
  }

  public void restore(Path zipPath) throws IOException {
    Files.createDirectories(DATA_DIR);
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        Path target = DATA_DIR.resolve(entry.getName());
        Path parent = target.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
        zis.closeEntry();
      }
    }
    UserDAO.getInstance().reload();
    AppointmentDAO.getInstance().reload();
    PaymentDAO.getInstance().reload();
    FeedbackDAO.getInstance().reload();
    ServiceDAO.getInstance().reload();
    AuditLogDAO.getInstance().reload();
  }
}
