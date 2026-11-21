package com.rubp.whattoeat.server.feature.auth.service;


import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.config.JwtProperties;
import com.rubp.whattoeat.server.feature.auth.exception.TokenIssuanceException;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            Clock clock
    ){
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String createToken(long id, Role role, JwtTokenType type) {
        return createJwt(id, role, type).getTokenValue();
    }

    public Jwt createJwt(long id, Role role, JwtTokenType type) {
        try {
            Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);

            Duration ttl = switch (type){
                case ACCESS -> jwtProperties.accessTokenTtl();
                case REFRESH -> jwtProperties.refreshTokenTtl();
            };

            return encodeJwt(id, role, type, now, now.plus(ttl));
        } catch (Exception exception) {
            throw new TokenIssuanceException(exception);
        }
    }

    public String createRefreshToken(long id, Role role, Instant expiresAt) {
        Instant now = Instant.now(clock);
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new JwtException("Refresh token is invalid or has expired");
        }

        try {
            return encodeJwt(id, role, JwtTokenType.REFRESH, now, expiresAt).getTokenValue();
        } catch (Exception exception) {
            throw new TokenIssuanceException(exception);
        }
    }

    public String hashRefreshToken(String refreshToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 hashing is unavailable", exception);
        }
    }

    private Jwt encodeJwt(long id, Role role, JwtTokenType type, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(Long.toString(id))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", role.name())
                .claim("token_type", type.name())
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.ES256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet));
    }
}
