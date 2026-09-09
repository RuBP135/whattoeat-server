package com.rubp.whattoeat.server.auth.service;


import com.rubp.whattoeat.server.auth.config.JwtProperties;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

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

    public String createToken(String uid, JwtTokenType type){

        Instant now = Instant.now(clock);

        Duration ttl = switch (type){
            case ACCESS -> jwtProperties.accessTokenTtl();
            case REFRESH -> jwtProperties.refreshTokenTtl();
        };

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(uid)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("token_type", type.name())
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.ES256)
                .type("JWT")
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claimsSet))
                .getTokenValue();
    }
}
