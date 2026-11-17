package com.rubp.whattoeat.server.feature.auth.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntry, Long> {
}
