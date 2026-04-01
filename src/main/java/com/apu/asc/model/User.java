package com.apu.asc.model;

import com.apu.asc.util.validation.Email;
import com.apu.asc.util.validation.NotBlank;
import com.apu.asc.util.validation.Pattern;
import com.apu.asc.util.validation.Size;

public abstract class User {

  private final String id;

  @NotBlank(message = "err.validation.usernameRequired")
  @Size(min = 3, max = 30, message = "err.validation.usernameSize|{min}|{max}")
  private final String username;

  private String passwordHash;
  private final Role role;
  private UserStatus status;

  @NotBlank(message = "err.validation.fullNameRequired")
  private String fullName;

  @NotBlank(message = "err.validation.emailRequired")
  @Email(message = "err.validation.emailInvalid")
  private String email;

  @NotBlank(message = "err.validation.contactRequired")
  @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "err.validation.contactInvalid")
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
