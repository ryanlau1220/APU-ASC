package com.apu.asc.service;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Customer;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import java.util.UUID;

public class AuthService implements IAuthService {

  private final UserDAO userDAO;

  public AuthService() {
    this.userDAO = UserDAO.getInstance();
  }

  public Result<User> login(String username, String rawPassword) {
    if (username == null || rawPassword == null)
      return Result.failure("err.auth.credentialsRequired");

    User user = userDAO.findByUsername(username.trim());
    if (user == null) return Result.failure("err.auth.invalidCredentials");
    if (user.getStatus() == UserStatus.DEACTIVATED) return Result.failure("err.auth.deactivated");
    if (!PasswordUtil.verify(rawPassword, user.getPasswordHash()))
      return Result.failure("err.auth.invalidCredentials");

    SessionManager.login(user);
    SystemLogger.log(
        user.getId(), "LOGIN", user.getId(), "User '" + user.getUsername() + "' logged in.");
    return Result.success(user);
  }

  public Result<Customer> register(
      String username, String rawPassword, String fullName, String email, String contactNumber) {
    username = DataSanitizer.clean(username).trim();
    fullName = DataSanitizer.clean(fullName).trim();
    email = DataSanitizer.clean(email).trim();
    contactNumber = DataSanitizer.clean(contactNumber).trim();

    if (userDAO.findByUsername(username) != null)
      return Result.failure("err.user.usernameTaken|" + username);

    String id = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String hash = PasswordUtil.hash(rawPassword);

    Customer customer =
        new Customer(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);

    String violations = ValidationUtil.getViolations(customer);
    if (violations != null) return Result.failure(violations);

    userDAO.save(customer);
    SystemLogger.log(id, "REGISTER", id, "New customer registered: '" + username + "'.");
    return Result.success(customer);
  }

  public boolean changePassword(User user, String currentRaw, String newRaw) {
    if (!PasswordUtil.verify(currentRaw, user.getPasswordHash())) return false;
    user.setPasswordHash(PasswordUtil.hash(newRaw));
    userDAO.save(user);
    SystemLogger.log(
        user.getId(),
        "CHANGE_PASSWORD",
        user.getId(),
        "User '" + user.getUsername() + "' changed their password.");
    return true;
  }

  public void logout() {
    User user = SessionManager.getCurrentUser();
    if (user != null) {
      SystemLogger.log(
          user.getId(), "LOGOUT", user.getId(), "User '" + user.getUsername() + "' logged out.");
    }
    SessionManager.logout();
  }
}
