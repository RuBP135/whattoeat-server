package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.auth.config.JwtProperties;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenServiceTest {

    private final JwtDecoder jwtAccessDecoder;
    private final JwtDecoder jwtRefreshDecoder;
    private final JwtProperties jwtProperties;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final JwtTokenService jwtTokenService;

    public JwtTokenServiceTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        jwtProperties = new JwtProperties(
                pemResource("PRIVATE KEY", keyPair.getPrivate().getEncoded()),
                pemResource("PUBLIC KEY", keyPair.getPublic().getEncoded()),
                Duration.ofMinutes(5),
                Duration.ofDays(30)
        );

        SecurityConfig securityConfig = new SecurityConfig();
        jwtAccessDecoder = securityConfig.jwtAccessDecoder(jwtProperties);
        jwtRefreshDecoder = securityConfig.jwtRefreshDecoder(jwtProperties);
        jwtTokenService = new JwtTokenService(
                securityConfig.jwtEncoder(jwtProperties),
                jwtProperties,
                clock
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {123L, Long.MAX_VALUE})
    public void createToken_shouldCreateValidToken(long id) {
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant accessExpiresAt = now.plus(jwtProperties.accessTokenTtl());
        Instant refreshExpiresAt = now.plus(jwtProperties.refreshTokenTtl());

        String accessToken = jwtTokenService.createToken(id, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenService.createToken(id, JwtTokenType.REFRESH);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        Jwt accessJwt = jwtAccessDecoder.decode(accessToken);
        Jwt refreshJwt = jwtRefreshDecoder.decode(refreshToken);

        assertEquals(Long.toString(id), accessJwt.getSubject());
        assertEquals(Long.toString(id), refreshJwt.getSubject());
        assertEquals(JwtTokenType.ACCESS, JwtTokenType.valueOf(accessJwt.getClaimAsString("token_type")));
        assertEquals(JwtTokenType.REFRESH, JwtTokenType.valueOf(refreshJwt.getClaimAsString("token_type")));
        assertEquals(now, accessJwt.getIssuedAt());
        assertEquals(now, refreshJwt.getIssuedAt());
        assertEquals(accessExpiresAt, accessJwt.getExpiresAt());
        assertEquals(refreshExpiresAt, refreshJwt.getExpiresAt());
    }

    private ByteArrayResource pemResource(String type, byte[] encodedKey) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getEncoder().encodeToString(encodedKey)
                + "\n-----END " + type + "-----";
        return new ByteArrayResource(pem.getBytes(StandardCharsets.US_ASCII));
    }
}
