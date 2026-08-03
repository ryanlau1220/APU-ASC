package com.apu.asc.user.internal;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByKeycloakId(String keycloakId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select user from UserEntity user where user.invitationTokenHash = :tokenHash")
  Optional<UserEntity> findByInvitationTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
