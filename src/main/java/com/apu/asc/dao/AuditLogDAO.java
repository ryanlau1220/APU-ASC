package com.apu.asc.dao;

import com.apu.asc.model.AuditLog;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class AuditLogDAO {

    private static final String FILE_PATH = "data/audit_logs.txt";
    private static AuditLogDAO instance;
    private final List<AuditLog> cache = new ArrayList<>();

    private AuditLogDAO() {
        load();
    }

    public static AuditLogDAO getInstance() {
        if (instance == null) {
            instance = new AuditLogDAO();
        }
        return instance;
    }

    private void load() {
        cache.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|\\|", -1);
                if (p.length < 6) continue;
                cache.add(new AuditLog(p[0], LocalDateTime.parse(p[1]), p[2], p[3], p[4], p[5]));
            }
        } catch (IOException e) {
            System.err.println("AuditLogDAO load failed: " + e.getMessage());
        }
    }

    public void append(AuditLog log) {
        cache.add(log);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(log.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("AuditLogDAO append failed: " + e.getMessage());
        }
    }

    public List<AuditLog> getAll() {
        return new ArrayList<>(cache);
    }
}
