package com.apu.asc.model;

public class Manager extends User {

    public Manager(String id, String username, String passwordHash,
                   UserStatus status, String fullName, String email, String contactNumber) {
        super(id, username, passwordHash, Role.MANAGER, status, fullName, email, contactNumber);
    }

    @Override
    public String getDashboardTitle() {
        return "Manager Dashboard";
    }
}
