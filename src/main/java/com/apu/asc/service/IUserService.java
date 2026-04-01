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

public interface IUserService {
  public boolean deactivateUser(String userId);
  public boolean reactivateUser(String userId);
  public User findById(String id);
  public List<User> getAllByRole(Role role);
  public List<User> getAll();
}
