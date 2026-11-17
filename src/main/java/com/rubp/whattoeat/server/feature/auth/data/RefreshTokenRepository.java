package com.rubp.whattoeat.server.feature.auth.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntry, Long> {

    Optional<RefreshTokenEntry> findByRefreshTokenHash(String refreshTokenHash);
}
