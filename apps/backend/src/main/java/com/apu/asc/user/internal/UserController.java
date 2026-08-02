package com.apu.asc.user.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration, retrieval, and status updates")
class UserController {

  private final UserApi userApi;

  @GetMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Get all system users", description = "Retrieves a complete list of users")
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userApi.findAllUsers());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF', 'CUSTOMER', 'TECHNICIAN')")
  @Operation(summary = "Get user by ID", description = "Retrieves user details by ID")
  public ResponseEntity<UserDto> getUserById(
      @PathVariable final String id, Authentication authentication) {
    UserDto user = userApi.getUserById(id);
    boolean isStaffOrManager =
        authentication.getAuthorities().stream()
            .anyMatch(
                a ->
                    a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_STAFF")
                        || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    if (!isStaffOrManager) {
      String principalName = authentication.getName();
      boolean isSelf =
          (user.username() != null && user.username().equalsIgnoreCase(principalName))
              || (user.id() != null && user.id().equals(principalName))
              || (user.keycloakId() != null && user.keycloakId().equals(principalName));
      if (!isSelf) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    }
    return ResponseEntity.ok(user);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Create user", description = "Registers a new user in the system")
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody final UserDto userDto) {
    UserDto created = userApi.createUser(userDto);
    return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF', 'CUSTOMER', 'TECHNICIAN')")
  @Operation(summary = "Update user details", description = "Updates user role or metadata")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable final String id,
      @Valid @RequestBody UserDto userDto,
      Authentication authentication) {
    UserDto existing = userApi.getUserById(id);
    boolean isManager =
        authentication.getAuthorities().stream()
            .anyMatch(
                a ->
                    a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    if (!isManager) {
      String principalName = authentication.getName();
      boolean isSelf =
          (existing.username() != null && existing.username().equalsIgnoreCase(principalName))
              || (existing.id() != null && existing.id().equals(principalName))
              || (existing.keycloakId() != null && existing.keycloakId().equals(principalName));
      if (!isSelf) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      // Non-managers cannot escalate roles or status
      userDto =
          new UserDto(
              existing.id(),
              existing.keycloakId(),
              existing.username(),
              userDto.email() != null ? userDto.email() : existing.email(),
              userDto.fullName() != null ? userDto.fullName() : existing.fullName(),
              existing.role(),
              existing.status(),
              existing.createdAt(),
              existing.updatedAt());
    }
    return ResponseEntity.ok(userApi.updateUser(id, userDto));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Update user status", description = "Activates or deactivates user account")
  public ResponseEntity<UserDto> updateStatus(
      @PathVariable final String id, @RequestParam final String status) {
    return ResponseEntity.ok(userApi.updateStatus(id, status));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(summary = "Deactivate user", description = "Deactivates specific user profile")
  public ResponseEntity<Void> deleteUser(@PathVariable final String id) {
    userApi.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
