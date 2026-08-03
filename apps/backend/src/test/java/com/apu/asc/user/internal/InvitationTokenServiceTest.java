package com.apu.asc.user.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvitationTokenServiceTest {

  private final InvitationTokenService invitationTokenService = new InvitationTokenService();

  @Test
  void issuesUniqueOpaqueTokensAndHashesOnlyTheStoredValue() {
    InvitationTokenService.InvitationToken first = invitationTokenService.issue();
    InvitationTokenService.InvitationToken second = invitationTokenService.issue();

    assertThat(first.rawToken()).hasSize(43);
    assertThat(first.rawToken()).isNotEqualTo(second.rawToken());
    assertThat(first.tokenHash()).hasSize(64);
    assertThat(invitationTokenService.hash(first.rawToken())).isEqualTo(first.tokenHash());
  }
}
