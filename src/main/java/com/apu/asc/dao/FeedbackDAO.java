package com.apu.asc.dao;

import com.apu.asc.model.Feedback;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FeedbackDAO {

    private static final String FILE_PATH = "data/feedbacks.txt";
    private static FeedbackDAO instance;
    private final Map<String, Feedback> cache = new LinkedHashMap<>();

    private FeedbackDAO() {
        load();
    }

    public static FeedbackDAO getInstance() {
        if (instance == null) {
            instance = new FeedbackDAO();
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
                if (p.length < 4) continue;
                Feedback f = new Feedback(p[0], p[1], Integer.parseInt(p[2]), p[3]);
                cache.put(f.getFeedbackId(), f);
            }
        } catch (IOException e) {
            System.err.println("FeedbackDAO load failed: " + e.getMessage());
        }
    }

    private void persist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Feedback f : cache.values()) {
                writer.write(f.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("FeedbackDAO persist failed: " + e.getMessage());
        }
    }

    public void save(Feedback feedback) {
        cache.put(feedback.getFeedbackId(), feedback);
        persist();
    }

    public Feedback findByAppointment(String appointmentId) {
        return cache.values().stream()
                .filter(f -> f.getAppointmentId().equals(appointmentId))
                .findFirst().orElse(null);
    }

    public List<Feedback> findByTechnician(String technicianId, AppointmentDAO appointmentDAO) {
        return cache.values().stream()
                .filter(f -> {
                    var appt = appointmentDAO.findById(f.getAppointmentId());
                    return appt != null && appt.getTechnicianId().equals(technicianId);
                })
                .collect(Collectors.toList());
    }

    public List<Feedback> getAll() {
        return new ArrayList<>(cache.values());
    }
}
