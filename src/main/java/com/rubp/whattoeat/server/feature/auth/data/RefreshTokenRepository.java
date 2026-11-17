package com.rubp.whattoeat.server.feature.auth.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntry, Long> {

    Optional<RefreshTokenEntry> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Transactional
    @Query("""
            update RefreshTokenEntry refreshToken
            set refreshToken.refreshTokenHash = :newHash
            where refreshToken.refreshTokenHash = :oldHash
              and refreshToken.refreshTokenHash <> :newHash
              and refreshToken.expiresAt > :now
            """)
    int replaceRefreshTokenHash(
            @Param("oldHash") String oldHash,
            @Param("newHash") String newHash,
            @Param("now") Instant now
    );
}
