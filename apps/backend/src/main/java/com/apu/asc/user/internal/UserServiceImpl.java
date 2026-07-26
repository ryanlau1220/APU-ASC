package com.apu.asc.user.internal;

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
  public UserDto getUserById(final String id) {
    return userRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findByUsername(final String username) {
    return userRepository.findByUsername(username).map(this::toDto);
  }

  @Override
  @Transactional
  public UserDto createUser(final UserDto userDto) {
    String id =
        userDto.id() != null
            ? userDto.id()
            : "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    UserEntity entity =
        UserEntity.builder()
            .id(id)
            .keycloakId(userDto.keycloakId())
            .username(userDto.username())
            .email(userDto.email())
            .fullName(userDto.fullName())
            .contactNumber(userDto.contactNumber())
            .role(userDto.role())
            .status(userDto.status() != null ? userDto.status() : "ACTIVE")
            .build();
    return toDto(userRepository.save(entity));
  }

  @Override
  @Transactional
  public UserDto syncJitUser(
      final String keycloakId,
      final String username,
      final String email,
      final String fullName,
      final String role) {
    Optional<UserEntity> existing =
        userRepository
            .findByKeycloakId(keycloakId)
            .or(() -> userRepository.findByUsername(username));

    if (existing.isPresent()) {
      UserEntity entity = existing.get();
      entity.setKeycloakId(keycloakId);
      if (email != null) entity.setEmail(email);
      if (fullName != null) entity.setFullName(fullName);
      if (role != null) entity.setRole(role);
      return toDto(userRepository.save(entity));
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
        entity.getContactNumber(),
        entity.getRole(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
