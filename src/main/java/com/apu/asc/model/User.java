package com.apu.asc.model;

public abstract class User {

    private final String id;
    private final String username;
    private String passwordHash;
    private final Role role;
    private UserStatus status;
    private String fullName;
    private String email;
    private String contactNumber;

    public User(String id, String username, String passwordHash, Role role,
                UserStatus status, String fullName, String email, String contactNumber) {
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

    public String getId()            { return id; }
    public String getUsername()      { return username; }
    public String getPasswordHash()  { return passwordHash; }
    public Role getRole()            { return role; }
    public UserStatus getStatus()    { return status; }
    public String getFullName()      { return fullName; }
    public String getEmail()         { return email; }
    public String getContactNumber() { return contactNumber; }

    public void setFullName(String fullName)           { this.fullName = fullName; }
    public void setEmail(String email)                 { this.email = email; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }
    public void setStatus(UserStatus status)           { this.status = status; }

    public String toFileString() {
        return String.join("||", id, username, passwordHash, role.name(),
                fullName, email, contactNumber, status.name());
    }
}
