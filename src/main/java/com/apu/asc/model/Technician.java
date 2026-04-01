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

  @Override
  public javax.swing.JPanel getDashboardPanel() {
    return new com.apu.asc.ui.technician.TechnicianJobPanel(this);
  }
}