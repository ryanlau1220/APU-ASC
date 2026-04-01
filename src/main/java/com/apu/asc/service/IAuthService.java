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

public interface IAuthService {
  public Result<User> login(String username, String rawPassword);
  public boolean changePassword(User user, String currentRaw, String newRaw);
  public void logout();
}
