package com.apu.asc.user.internal;

import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserApi {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAllUsers() {
    return userRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserById(String id) {
    return userRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findById(String id) {
    return userRepository.findById(id).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByEmail(String email) {
    return userRepository.findByEmail(email).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByUsername(String username) {
    return userRepository.findByUsername(username).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByKeycloakId(String keycloakId) {
    return userRepository.findByKeycloakId(keycloakId).map(this::toDto);
  }

  @Override
  @Transactional
  public UserDto createUser(UserDto userDto) {
    String generatedId =
        userDto.id() != null
            ? userDto.id()
            : "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    UserEntity entity =
        UserEntity.builder()
            .id(generatedId)
            .keycloakId(userDto.keycloakId())
            .username(userDto.username())
            .email(userDto.email())
            .fullName(userDto.fullName())
            .role(userDto.role() != null ? userDto.role() : "CUSTOMER")
            .status(userDto.status() != null ? userDto.status() : "ACTIVE")
            .build();
    return toDto(userRepository.save(entity));
  }

  @Override
  @Transactional
  public UserDto updateUser(String id, UserDto userDto) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

    if (userDto.fullName() != null) entity.setFullName(userDto.fullName());
    if (userDto.email() != null) entity.setEmail(userDto.email());
    if (userDto.role() != null) entity.setRole(userDto.role());
    if (userDto.status() != null) entity.setStatus(userDto.status());

    return toDto(userRepository.save(entity));
  }

  @Override
  @Transactional
  public UserDto updateStatus(String id, String status) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    entity.setStatus(status);
    return toDto(userRepository.save(entity));
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    UserEntity entity =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    entity.setStatus("INACTIVE");
    userRepository.save(entity);
  }

  @Override
  @Transactional
  public UserDto syncJitUser(
      String keycloakId, String username, String email, String fullName, String role) {
    Optional<UserEntity> existing = userRepository.findByKeycloakId(keycloakId);
    if (existing.isPresent()) {
      UserEntity user = existing.get();
      boolean updated = false;
      if (email != null && !email.equals(user.getEmail())) {
        user.setEmail(email);
        updated = true;
      }
      if (fullName != null && !fullName.equals(user.getFullName())) {
        user.setFullName(fullName);
        updated = true;
      }
      if (role != null && !role.equals(user.getRole())) {
        user.setRole(role);
        updated = true;
      }
      if (updated) {
        userRepository.save(user);
      }
      return toDto(user);
    }

    String generatedId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    UserEntity newEntity =
        UserEntity.builder()
            .id(generatedId)
            .keycloakId(keycloakId)
            .username(username != null ? username : keycloakId)
            .email(email != null ? email : username + "@apu-asc.com")
            .fullName(fullName != null ? fullName : username)
            .role(role != null ? role : "CUSTOMER")
            .status("ACTIVE")
            .build();
    return toDto(userRepository.save(newEntity));
  }

  private UserDto toDto(UserEntity entity) {
    return new UserDto(
        entity.getId(),
        entity.getKeycloakId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getFullName(),
        entity.getRole(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
