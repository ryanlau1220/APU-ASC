package com.apu.asc.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

class SseControllerMetricsTest {

  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final CurrentUserService currentUserService = new CurrentUserService(new TestUserApi());
  private final SseController controller =
      new SseController(currentUserService, event -> {}, meterRegistry);

  @AfterEach
  void shutdownController() {
    controller.shutdownHeartbeat();
    meterRegistry.close();
  }

  @Test
  @DisplayName("Tracks active subscribers and successful live-update delivery")
  void tracksSubscriberAndDeliveryMetrics() {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("USR-101", "not-used", "ROLE_MANAGER");

    controller.subscribe(authentication);
    controller.handleLiveUpdateEvent(LiveUpdateEvent.forRoles("work-orders", "WO-101", "MANAGER"));

    assertThat(meterRegistry.get("sse.subscribers.active").gauge().value()).isEqualTo(1.0);
    assertThat(meterRegistry.counter("sse.live_update.delivered").count()).isEqualTo(1.0);
    assertThat(meterRegistry.counter("sse.live_update.delivery.failure").count()).isZero();
  }

  private static class TestUserApi implements UserApi {
    private static final UserDto USER =
        new UserDto(
            "USR-101",
            "keycloak-101",
            "manager",
            "manager@example.test",
            "Manager",
            "MANAGER",
            "ACTIVE",
            null,
            null,
            null);

    @Override
    public List<UserDto> findAllUsers() {
      return List.of(USER);
    }

    @Override
    public UserDto getUserById(String id) {
      return USER;
    }

    @Override
    public Optional<UserDto> findById(String id) {
      return Optional.of(USER);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
      return Optional.empty();
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
      return Optional.empty();
    }

    @Override
    public Optional<UserDto> findByKeycloakId(String keycloakId) {
      return Optional.empty();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UserDto updateUser(String id, UserDto userDto) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UserDto updateStatus(String id, String status) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UserDto reissueEmployeeInvitation(String id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(String id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void hardDeleteUser(String id) {
      throw new UnsupportedOperationException();
    }

    @Override
    public UserDto syncJitUser(
        String keycloakId, String username, String email, String fullName, String role) {
      throw new UnsupportedOperationException();
    }
  }
}
