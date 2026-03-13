package com.apu.asc.dao;

import com.apu.asc.model.Service;
import com.apu.asc.model.ServiceType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceDAO {

    private static final String FILE_PATH = "data/services.txt";
    private static ServiceDAO instance;
    private final Map<String, Service> cache = new LinkedHashMap<>();

    private ServiceDAO() {
        load();
    }

    public static ServiceDAO getInstance() {
        if (instance == null) {
            instance = new ServiceDAO();
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
                Service s = new Service(p[0], ServiceType.valueOf(p[1]), p[2],
                        Double.parseDouble(p[3]), Boolean.parseBoolean(p[4]));
                cache.put(s.getServiceId(), s);
            }
        } catch (IOException e) {
            System.err.println("ServiceDAO load failed: " + e.getMessage());
        }
    }

    private void persist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Service s : cache.values()) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("ServiceDAO persist failed: " + e.getMessage());
        }
    }

    public void save(Service service) {
        cache.put(service.getServiceId(), service);
        persist();
    }

    public Service findById(String id) {
        return cache.get(id);
    }

    public List<Service> getActiveServices() {
        return cache.values().stream()
                .filter(Service::isActive)
                .collect(Collectors.toList());
    }

    public List<Service> getAll() {
        return new ArrayList<>(cache.values());
    }
}
