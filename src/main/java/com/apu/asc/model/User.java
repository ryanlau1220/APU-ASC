package com.apu.asc.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public abstract class User {

  private final String id;

  @NotBlank(message = "Username cannot be empty")
  @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
  private final String username;

  private String passwordHash;
  private final Role role;
  private UserStatus status;

  @NotBlank(message = "Full name cannot be empty")
  private String fullName;

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Contact number cannot be empty")
  @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid contact number format")
  private String contactNumber;

  public User(
      String id,
      String username,
      String passwordHash,
      Role role,
      UserStatus status,
      String fullName,
      String email,
      String contactNumber) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.status = status;
    this.fullName = fullName;
    this.email = email;
    this.contactNumber = contactNumber;
  }

  public abstract String getDashboardTitle();

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getContactNumber() {
    return contactNumber;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setContactNumber(String contactNumber) {
    this.contactNumber = contactNumber;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String toFileString() {
    return String.join(
        "||",
        id,
        username,
        passwordHash,
        role.name(),
        fullName,
        email,
        contactNumber,
        status.name());
  }
}
