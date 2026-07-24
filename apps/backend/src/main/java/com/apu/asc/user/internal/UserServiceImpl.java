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
