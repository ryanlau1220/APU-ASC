package com.apu.asc.dao;

import com.apu.asc.model.Payment;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class PaymentDAO {

    private static final String FILE_PATH = "data/payments.txt";
    private static PaymentDAO instance;
    private final Map<String, Payment> cache = new LinkedHashMap<>();

    private PaymentDAO() {
        load();
    }

    public static PaymentDAO getInstance() {
        if (instance == null) {
            instance = new PaymentDAO();
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
                if (p.length < 5) continue;
                Payment payment = new Payment(p[0], p[1], Double.parseDouble(p[2]),
                        LocalDateTime.parse(p[3]), Boolean.parseBoolean(p[4]));
                cache.put(payment.getPaymentId(), payment);
            }
        } catch (IOException e) {
            System.err.println("PaymentDAO load failed: " + e.getMessage());
        }
    }

    private void persist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Payment payment : cache.values()) {
                writer.write(payment.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("PaymentDAO persist failed: " + e.getMessage());
        }
    }

    public void save(Payment payment) {
        cache.put(payment.getPaymentId(), payment);
        persist();
    }

    public Payment findByAppointment(String appointmentId) {
        return cache.values().stream()
                .filter(p -> p.getAppointmentId().equals(appointmentId))
                .findFirst().orElse(null);
    }

    public List<Payment> getAll() {
        return new ArrayList<>(cache.values());
    }
}
