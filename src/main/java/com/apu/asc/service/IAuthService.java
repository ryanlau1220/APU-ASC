package com.apu.asc.service;

import com.apu.asc.model.User;
import com.apu.asc.util.Result;

public interface IAuthService {
  public Result<User> login(String username, String rawPassword);
  public boolean changePassword(User user, String currentRaw, String newRaw);
  public void logout();
}
