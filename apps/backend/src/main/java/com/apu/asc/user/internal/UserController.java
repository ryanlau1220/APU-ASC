package com.apu.asc.user.internal;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

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
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), user.id());
    return ResponseEntity.ok(user);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
  @Operation(summary = "Create user", description = "Registers a new user in the system")
  public ResponseEntity<UserDto> createUser(
      @Valid @RequestBody final UserDto userDto, Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    UserDto securedUser = userDto;
    if (!accessPolicy.isManager(currentUser)) {
      securedUser =
          new UserDto(
              null,
              null,
              userDto.username(),
              userDto.email(),
              userDto.fullName(),
              "CUSTOMER",
              "ACTIVE",
              null,
              null);
    }
    UserDto created = userApi.createUser(securedUser);
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
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    if (!accessPolicy.isManager(currentUser)) {
      accessPolicy.requireSelfOrOperational(currentUser, existing.id());
      // Only managers can alter an account's role or activation state.
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
  @Operation(
      summary = "Update user status",
      operationId = "updateUserStatus",
      description = "Activates or deactivates user account")
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
