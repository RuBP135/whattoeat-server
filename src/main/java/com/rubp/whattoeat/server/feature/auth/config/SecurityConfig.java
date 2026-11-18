package com.rubp.whattoeat.server.feature.auth.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            @Qualifier("jwtAccessDecoder") JwtDecoder jwtAccessDecoder,
            ObjectMapper objectMapper
    ) {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    new ErrorResponse(
                                            "INVALID_ACCESS_TOKEN",
                                            "access token 无效或已过期"
                                    )
                            );
                        })
                        .jwt(jwt -> jwt
                                .decoder(jwtAccessDecoder)
                                .jwtAuthenticationConverter(authenticationConverter)
                        )
                )
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {

        ECPublicKey publicKey = publicKey(jwtProperties);
        ECPrivateKey privateKey = privateKey(jwtProperties);

        return NimbusJwtEncoder
                .withKeyPair(publicKey, privateKey)
                .build();
    }


    @Bean
    public JwtDecoder jwtAccessDecoder(JwtProperties jwtProperties) {
        return jwtDecoder(jwtProperties, "ACCESS");
    }

    @Bean
    public JwtDecoder jwtRefreshDecoder(JwtProperties jwtProperties) {
        return jwtDecoder(jwtProperties, "REFRESH");
    }



    private JwtDecoder jwtDecoder(JwtProperties jwtProperties, String tokenType){

        ECKey key = verificationKey(jwtProperties);
        JWKSet keySet = new JWKSet(key);

        JWKSource<SecurityContext> jwkSource =
                (selector, context) ->
                        selector.select(keySet);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSource(jwkSource)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();

        var defaultConverter = MappedJwtClaimSetConverter.withDefaults(Map.of());
        decoder.setClaimSetConverter(claims -> {
            if (claims.get(JwtClaimNames.IAT) == null) {
                throw new BadJwtException("JWT is missing the iat claim");
            }
            return defaultConverter.convert(claims);
        });

        JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        timestampValidator.setAllowEmptyExpiryClaim(false);

        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
                timestampValidator,
                new JwtTypeValidator("JWT"),
                new JwtClaimValidator<String>(JwtClaimNames.SUB, StringUtils::hasText),
                new JwtClaimValidator<>(JwtClaimNames.IAT, Instant.class::isInstance),
                new JwtClaimValidator<String>("token_type", tokenType::equals),
                new JwtClaimValidator<>("role", value ->
                        Role.USER.name().equals(value) || Role.ADMIN.name().equals(value))
        )));

        return decoder;
    }

    private ECPublicKey publicKey(JwtProperties jwtProperties){

        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Failed to create EC key factory",
                    exception);
        }

        try{
            return (ECPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(readPem(jwtProperties.publicKeyLocation(), "PUBLIC KEY"))
            );

        } catch (InvalidKeySpecException exception){
            throw new IllegalStateException(
                    "Failed to parse EC public key",
                    exception
            );
        }
    }

    private ECPrivateKey privateKey(JwtProperties jwtProperties){

        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Failed to create EC key factory",
                    exception);
        }

        try{
            return (ECPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(readPem(jwtProperties.privateKeyLocation(), "PRIVATE KEY"))
            );

        } catch (InvalidKeySpecException exception){
            throw new IllegalStateException(
                    "Failed to parse EC private key",
                    exception
            );
        }
    }

    private ECKey verificationKey(JwtProperties jwtProperties){

        try{
            return new ECKey.Builder(Curve.P_256, publicKey(jwtProperties))
                    .algorithm(JWSAlgorithm.ES256)
                    .keyIDFromThumbprint()
                    .build();

        } catch (JOSEException exception){
            throw new IllegalStateException(
                    "Failed to compute EC JWK thumbprint to generate kid",
                    exception
            );
        } catch (IllegalArgumentException exception){
            throw new IllegalStateException(
                    "Invalid parameters for the EC verification key",
                    exception
            );
        }

    }

    private byte[] readPem(Resource resource, String type){
        String pem;

        try {
            pem = resource
                    .getContentAsString(StandardCharsets.US_ASCII)
                    .trim();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to read key file. Check that it exists and is readable.",
                    exception
            );
        }

        String beginMarker = "-----BEGIN " + type + "-----";
        String endMarker = "-----END " + type + "-----";

        if(!pem.startsWith(beginMarker) || !pem.endsWith(endMarker)){
            throw new IllegalStateException(
                    "Key file must use the expected PEM format"
            );
        }

        String encodedKey = pem.substring(
                beginMarker.length(),
                pem.length() - endMarker.length()
        ).replaceAll("\\s", "");

        try {
            return Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception){
            throw new IllegalStateException(
                    "Key file contains invalid Base64 data",
                    exception
            );
        }
    }
}
