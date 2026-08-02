package com.apu.asc.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserServiceImplTest {

  @Test
  void userDtoCreation_shouldHoldValidFields() {
    UserDto userDto =
        new UserDto(
            "USR-001",
            "kc-123",
            "john_doe",
            "john@example.com",
            "John Doe",
            "CUSTOMER",
            "ACTIVE",
            null,
            null,
            null);

    assertThat(userDto.id()).isEqualTo("USR-001");
    assertThat(userDto.username()).isEqualTo("john_doe");
    assertThat(userDto.role()).isEqualTo("CUSTOMER");
  }
}
