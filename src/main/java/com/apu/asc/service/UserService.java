package com.apu.asc.service;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.CounterStaff;
import com.apu.asc.model.Customer;
import com.apu.asc.model.Manager;
import com.apu.asc.model.Role;
import com.apu.asc.model.Technician;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import java.util.List;
import java.util.UUID;

public class UserService {

  private final UserDAO userDAO;

  public UserService() {
    this.userDAO = UserDAO.getInstance();
  }

  public Result<User> createUser(
      Role role,
      String username,
      String rawPassword,
      String fullName,
      String email,
      String contactNumber) {
    username = DataSanitizer.clean(username).trim();
    fullName = DataSanitizer.clean(fullName).trim();
    email = DataSanitizer.clean(email).trim();
    contactNumber = DataSanitizer.clean(contactNumber).trim();

    if (userDAO.findByUsername(username) != null)
      return Result.failure("Username '" + username + "' is already taken. Choose another.");

    String id = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String hash = PasswordUtil.hash(rawPassword);
    User user = buildUser(role, id, username, hash, fullName, email, contactNumber);

    String violations = ValidationUtil.getViolations(user);
    if (violations != null) return Result.failure(violations);

    userDAO.save(user);
    String actor = actorId();
    SystemLogger.log(actor, "CREATE_USER", id, "Created " + role + " account: '" + username + "'.");
    return Result.success(user);
  }

  public Result<User> updateProfile(
      User user, String fullName, String email, String contactNumber) {
    user.setFullName(DataSanitizer.clean(fullName).trim());
    user.setEmail(DataSanitizer.clean(email).trim());
    user.setContactNumber(DataSanitizer.clean(contactNumber).trim());

    String violations = ValidationUtil.getViolations(user);
    if (violations != null) return Result.failure(violations);

    userDAO.save(user);
    SystemLogger.log(
        actorId(),
        "UPDATE_PROFILE",
        user.getId(),
        "Profile updated for '" + user.getUsername() + "'.");
    return Result.success(user);
  }

  public boolean deactivateUser(String userId) {
    User user = userDAO.findById(userId);
    if (user == null || user.getStatus() == UserStatus.DEACTIVATED) return false;
    userDAO.deactivate(userId);
    SystemLogger.log(
        actorId(), "DEACTIVATE_USER", userId, "Deactivated user '" + user.getUsername() + "'.");
    return true;
  }

  public boolean reactivateUser(String userId) {
    User user = userDAO.findById(userId);
    if (user == null || user.getStatus() == UserStatus.ACTIVE) return false;
    user.setStatus(UserStatus.ACTIVE);
    userDAO.save(user);
    SystemLogger.log(
        actorId(), "REACTIVATE_USER", userId, "Reactivated user '" + user.getUsername() + "'.");
    return true;
  }

  public User findById(String id) {
    return userDAO.findById(id);
  }

  public List<User> getAllByRole(Role role) {
    return userDAO.getAllByRole(role);
  }

  public List<User> getAll() {
    return userDAO.getAll();
  }

  private User buildUser(
      Role role,
      String id,
      String username,
      String hash,
      String fullName,
      String email,
      String contactNumber) {
    return switch (role) {
      case CUSTOMER ->
          new Customer(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);
      case STAFF ->
          new CounterStaff(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);
      case TECHNICIAN ->
          new Technician(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);
      case MANAGER ->
          new Manager(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);
    };
  }

  private String actorId() {
    User u = SessionManager.getCurrentUser();
    return u != null ? u.getId() : "SYSTEM";
  }
}
