package com.apu.asc.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.apu.asc.common.security.AuthenticatedUser;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveUpdateEventTest {

  @Test
  @DisplayName("Should match a subscriber by user ID or role")
  void shouldMatchScopedAudience() {
    LiveUpdateEvent event =
        LiveUpdateEvent.forUsersAndRoles("work-orders", "WO-1", Set.of("CUS-1"), Set.of("STAFF"));

    assertThat(event.isVisibleTo(new AuthenticatedUser("CUS-1", Set.of("CUSTOMER")))).isTrue();
    assertThat(event.isVisibleTo(new AuthenticatedUser("STAFF-1", Set.of("STAFF")))).isTrue();
    assertThat(event.isVisibleTo(new AuthenticatedUser("CUS-2", Set.of("CUSTOMER")))).isFalse();
  }

  @Test
  @DisplayName("Should ignore blank optional recipients while preserving the valid audience")
  void shouldNormalizeOptionalRecipients() {
    LiveUpdateEvent event =
        LiveUpdateEvent.forUsersAndRoles(
            "appointments", "APT-1", java.util.Arrays.asList("CUS-1", null, " "), Set.of("staff"));

    assertThat(event.audienceUserIds()).containsExactly("CUS-1");
    assertThat(event.audienceRoles()).containsExactly("STAFF");
  }
}
