package com.apu.asc.dao;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import java.util.List;

public interface IUserDAO {
  void save(User user);
  User findById(String id);
  User findByUsername(String username);
  List<User> getAll();
  List<User> getAllByRole(Role role);
  void deactivate(String id);
  void reload();
}
