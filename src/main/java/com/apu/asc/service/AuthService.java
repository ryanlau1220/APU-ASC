package com.apu.asc.service;

import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Customer;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;

import java.util.UUID;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = UserDAO.getInstance();
    }

    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        User user = userDAO.findByUsername(username.trim());
        if (user == null) return null;
        if (user.getStatus() == UserStatus.DEACTIVATED) return null;
        if (!PasswordUtil.verify(rawPassword, user.getPasswordHash())) return null;

        SessionManager.login(user);
        SystemLogger.log(user.getId(), "LOGIN", user.getId(),
                "User '" + user.getUsername() + "' logged in.");
        return user;
    }

    public Customer register(String username, String rawPassword,
                             String fullName, String email, String contactNumber) {
        username      = DataSanitizer.clean(username).trim();
        fullName      = DataSanitizer.clean(fullName).trim();
        email         = DataSanitizer.clean(email).trim();
        contactNumber = DataSanitizer.clean(contactNumber).trim();

        if (userDAO.findByUsername(username) != null) return null;

        String id   = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String hash = PasswordUtil.hash(rawPassword);

        Customer customer = new Customer(id, username, hash, UserStatus.ACTIVE, fullName, email, contactNumber);
        userDAO.save(customer);
        SystemLogger.log(id, "REGISTER", id,
                "New customer registered: '" + username + "'.");
        return customer;
    }

    public boolean changePassword(User user, String currentRaw, String newRaw) {
        if (!PasswordUtil.verify(currentRaw, user.getPasswordHash())) return false;
        user.setPasswordHash(PasswordUtil.hash(newRaw));
        userDAO.save(user);
        SystemLogger.log(user.getId(), "CHANGE_PASSWORD", user.getId(),
                "User '" + user.getUsername() + "' changed their password.");
        return true;
    }

    public void logout() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            SystemLogger.log(user.getId(), "LOGOUT", user.getId(),
                    "User '" + user.getUsername() + "' logged out.");
        }
        SessionManager.logout();
    }
}
