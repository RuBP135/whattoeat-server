package com.rubp.whattoeat.server.auth.service;

import com.rubp.whattoeat.server.auth.config.JwtProperties;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtTokenServiceTest {

    private final JwtDecoder jwtAccessDecoder;
    private final JwtDecoder jwtRefreshDecoder;
    private final JwtProperties jwtProperties;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final JwtTokenService jwtTokenService;


    public JwtTokenServiceTest(
            JwtEncoder jwtEncoder,
            @Qualifier("jwtAccessDecoder") JwtDecoder jwtAccessDecoder,
            @Qualifier("jwtRefreshDecoder") JwtDecoder jwtRefreshDecoder,
            JwtProperties jwtProperties
    ) {
        this.jwtAccessDecoder = jwtAccessDecoder;
        this.jwtRefreshDecoder = jwtRefreshDecoder;
        this.jwtProperties = jwtProperties;


        jwtTokenService = new JwtTokenService(
                jwtEncoder,
                jwtProperties,
                clock
        );
    }

    @Test
    public void createToken_shouldCreateValidToken() {
        String uid = "1234567890";
        Instant now = Instant.now(clock);
        Instant accessExpiresAt = now.plus(jwtProperties.accessTokenTtl());
        Instant refreshExpiresAt = now.plus(jwtProperties.refreshTokenTtl());

        String accessToken = jwtTokenService.createToken(uid, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenService.createToken(uid, JwtTokenType.REFRESH);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        Jwt accessJwt = jwtAccessDecoder.decode(accessToken);
        Jwt refreshJwt = jwtRefreshDecoder.decode(refreshToken);

        assertEquals(uid, accessJwt.getSubject());
        assertEquals(uid, refreshJwt.getSubject());
        assertEquals(JwtTokenType.ACCESS, JwtTokenType.valueOf(accessJwt.getClaimAsString("token_type")));
        assertEquals(JwtTokenType.REFRESH, JwtTokenType.valueOf(refreshJwt.getClaimAsString("token_type")));
        assertEquals(now, accessJwt.getIssuedAt());
        assertEquals(now, refreshJwt.getIssuedAt());
        assertEquals(accessExpiresAt, accessJwt.getExpiresAt());
        assertEquals(refreshExpiresAt, refreshJwt.getExpiresAt());
    }
}