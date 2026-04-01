package com.apu.asc.model;

public class Customer extends User {

  public Customer(
      String id,
      String username,
      String passwordHash,
      UserStatus status,
      String fullName,
      String email,
      String contactNumber) {
    super(id, username, passwordHash, Role.CUSTOMER, status, fullName, email, contactNumber);
  }

  @Override
  public String getDashboardTitle() {
    return "Customer Dashboard";
  }

  @Override
  public javax.swing.JPanel getDashboardPanel() {
    return new com.apu.asc.ui.customer.CustomerHistoryPanel(this);
  }
}