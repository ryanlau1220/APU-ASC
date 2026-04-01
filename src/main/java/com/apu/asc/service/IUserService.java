package com.apu.asc.service;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import java.util.List;

public interface IUserService {
  public boolean deactivateUser(String userId);
  public boolean reactivateUser(String userId);
  public User findById(String id);
  public List<User> getAllByRole(Role role);
  public List<User> getAll();
}
