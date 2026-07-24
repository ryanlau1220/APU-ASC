package com.apu.asc.user.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByKeycloakId(String keycloakId);
}
