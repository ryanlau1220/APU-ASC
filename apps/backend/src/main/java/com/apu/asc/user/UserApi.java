package com.apu.asc.user;

import java.util.List;
import java.util.Optional;

public interface UserApi {
  List<UserDto> findAllUsers();

  UserDto getUserById(String id);

  Optional<UserDto> findByUsername(String username);

  UserDto createUser(UserDto userDto);

  UserDto syncJitUser(
      String keycloakId, String username, String email, String fullName, String role);
}
