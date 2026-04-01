package com.apu.asc.model;

public class CounterStaff extends User {

  public CounterStaff(
      String id,
      String username,
      String passwordHash,
      UserStatus status,
      String fullName,
      String email,
      String contactNumber) {
    super(id, username, passwordHash, Role.STAFF, status, fullName, email, contactNumber);
  }

  @Override
  public String getDashboardTitle() {
    return "Counter Staff Dashboard";
  }

  @Override
  public javax.swing.JPanel getDashboardPanel() {
    return new com.apu.asc.ui.staff.StaffCustomerPanel();
  }
}