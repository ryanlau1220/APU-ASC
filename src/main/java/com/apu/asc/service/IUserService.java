package com.apu.asc.service;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.util.Result;
import java.util.List;

public interface IUserService {
  public Result<User> createUser(Role role, String username, String rawPassword, String fullName, String email, String contactNumber);
  public Result<User> updateProfile(User user, String fullName, String email, String contactNumber);
  public boolean deactivateUser(String userId);
  public boolean reactivateUser(String userId);
  public User findById(String id);
  public List<User> getAllByRole(Role role);
  public List<User> getAll();
}
