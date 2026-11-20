package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.config.JwtProperties;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JoseHeaderNames;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenServiceTest {

    private final JwtDecoder jwtAccessDecoder;
    private final JwtDecoder jwtRefreshDecoder;
    private final JwtProperties jwtProperties;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final JwtEncoder jwtEncoder;
    private final JwtTokenService jwtTokenService;

    public JwtTokenServiceTest() throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();

        jwtProperties = new JwtProperties(
                keys.privateKey(),
                keys.publicKey(),
                Duration.ofMinutes(5),
                Duration.ofDays(30)
        );

        SecurityConfig securityConfig = new SecurityConfig();
        jwtAccessDecoder = securityConfig.jwtAccessDecoder(jwtProperties);
        jwtRefreshDecoder = securityConfig.jwtRefreshDecoder(jwtProperties);
        jwtEncoder = securityConfig.jwtEncoder(jwtProperties);
        jwtTokenService = new JwtTokenService(
                jwtEncoder,
                jwtProperties,
                clock
        );
    }

    @ParameterizedTest
    @CsvSource({"123, USER", Long.MAX_VALUE + ", ADMIN"})
    public void createToken_shouldCreateValidToken(long id, Role role) {
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant accessExpiresAt = now.plus(jwtProperties.accessTokenTtl());
        Instant refreshExpiresAt = now.plus(jwtProperties.refreshTokenTtl());

        String accessToken = jwtTokenService.createToken(id, role, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenService.createToken(id, role, JwtTokenType.REFRESH);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        Jwt accessJwt = jwtAccessDecoder.decode(accessToken);
        Jwt refreshJwt = jwtRefreshDecoder.decode(refreshToken);

        assertEquals(Long.toString(id), accessJwt.getSubject());
        assertEquals(Long.toString(id), refreshJwt.getSubject());
        assertEquals(role.name(), accessJwt.getClaimAsString("role"));
        assertEquals(role.name(), refreshJwt.getClaimAsString("role"));
        assertEquals(JwtTokenType.ACCESS, JwtTokenType.valueOf(accessJwt.getClaimAsString("token_type")));
        assertEquals(JwtTokenType.REFRESH, JwtTokenType.valueOf(refreshJwt.getClaimAsString("token_type")));
        assertEquals(now, accessJwt.getIssuedAt());
        assertEquals(now, refreshJwt.getIssuedAt());
        assertEquals(accessExpiresAt, accessJwt.getExpiresAt());
        assertEquals(refreshExpiresAt, refreshJwt.getExpiresAt());
    }

    @ParameterizedTest(name = "缺少 {0}")
    @ValueSource(strings = {
            JwtClaimNames.SUB, JwtClaimNames.IAT, JwtClaimNames.EXP, "token_type", "role", JoseHeaderNames.TYP
    })
    public void decode_shouldRejectTokenMissingRequiredField(String field) {
        String accessToken = createSignedTokenWithoutField(JwtTokenType.ACCESS, field);
        String refreshToken = createSignedTokenWithoutField(JwtTokenType.REFRESH, field);

        assertAll(
                () -> assertThrows(JwtException.class, () -> jwtAccessDecoder.decode(accessToken), "Access Token"),
                () -> assertThrows(JwtException.class, () -> jwtRefreshDecoder.decode(refreshToken), "Refresh Token")
        );
    }

    @Test
    public void decode_shouldRejectTokenOfDifferentType() {
        String accessToken = jwtTokenService.createToken(123L, Role.USER, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenService.createToken(123L, Role.USER, JwtTokenType.REFRESH);

        assertAll(
                () -> assertThrows(JwtException.class, () -> jwtRefreshDecoder.decode(accessToken)),
                () -> assertThrows(JwtException.class, () -> jwtAccessDecoder.decode(refreshToken))
        );
    }

    private String createSignedTokenWithoutField(JwtTokenType tokenType, String field) {
        Jwt original = jwtTokenService.createJwt(123L, Role.USER, tokenType);
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.ES256)
                .headers(headers -> {
                    headers.putAll(original.getHeaders());
                    headers.remove(field);
                })
                .build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .claims(values -> {
                    values.putAll(original.getClaims());
                    values.remove(field);
                })
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
