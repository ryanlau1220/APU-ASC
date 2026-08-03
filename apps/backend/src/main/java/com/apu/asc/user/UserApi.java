package com.apu.asc.user;

import java.util.List;
import java.util.Optional;

public interface UserApi {
  List<UserDto> findAllUsers();

  UserDto getUserById(String id);

  Optional<UserDto> findById(String id);

  Optional<UserDto> findByEmail(String email);

  Optional<UserDto> findByUsername(String username);

  Optional<UserDto> findByKeycloakId(String keycloakId);

  UserDto createUser(UserDto userDto);

  UserDto updateUser(String id, UserDto userDto);

  UserDto updateStatus(String id, String status);

  UserDto reissueEmployeeInvitation(String id);

  void deleteUser(String id);

  void hardDeleteUser(String id);

  UserDto syncJitUser(
      String keycloakId, String username, String email, String fullName, String role);
}
