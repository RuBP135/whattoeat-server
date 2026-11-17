package com.rubp.whattoeat.server.feature.auth.service;


import com.rubp.whattoeat.server.feature.auth.config.JwtProperties;
import com.rubp.whattoeat.server.feature.auth.exception.TokenIssuanceException;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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

    public String createToken(long id, JwtTokenType type) {
        return createJwt(id, type).getTokenValue();
    }

    public Jwt createJwt(long id, JwtTokenType type) {
        try {
            Instant now = Instant.now(clock);

            Duration ttl = switch (type){
                case ACCESS -> jwtProperties.accessTokenTtl();
                case REFRESH -> jwtProperties.refreshTokenTtl();
            };

            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                    .subject(Long.toString(id))
                    .issuedAt(now)
                    .expiresAt(now.plus(ttl))
                    .claim("token_type", type.name())
                    .build();

            JwsHeader header = JwsHeader
                    .with(SignatureAlgorithm.ES256)
                    .type("JWT")
                    .build();

            return jwtEncoder
                    .encode(JwtEncoderParameters.from(header, claimsSet));
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
            throw new IllegalStateException("无法使用 SHA-256 哈希算法", exception);
        }
    }
}
