package com.apu.asc.model;

public class Technician extends User {

  public Technician(
      String id,
      String username,
      String passwordHash,
      UserStatus status,
      String fullName,
      String email,
      String contactNumber) {
    super(id, username, passwordHash, Role.TECHNICIAN, status, fullName, email, contactNumber);
  }

  @Override
  public String getDashboardTitle() {
    return "Technician Dashboard";
  }
}
