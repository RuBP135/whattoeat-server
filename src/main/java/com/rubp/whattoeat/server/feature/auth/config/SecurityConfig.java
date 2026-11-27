package com.rubp.whattoeat.server.feature.auth.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.rubp.whattoeat.server.core.crypto.KeyFileReader;
import com.rubp.whattoeat.server.core.error.ErrorResponseFactory;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            @Qualifier("jwtAccessDecoder") JwtDecoder jwtAccessDecoder,
            ObjectMapper objectMapper,
            ErrorResponseFactory errorResponseFactory
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
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/refresh",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/accounts/register/start/email",
                                "/api/accounts/register/verify/email",
                                "/api/accounts/register/complete/email"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint((request, response, exception) -> writeErrorResponse(
                                request,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                objectMapper,
                                errorResponseFactory,
                                "INVALID_ACCESS_TOKEN",
                                "auth.invalid_access_token"
                        ))
                        .accessDeniedHandler((request, response, exception) -> writeErrorResponse(
                                request,
                                response,
                                HttpStatus.FORBIDDEN,
                                objectMapper,
                                errorResponseFactory,
                                "ACCESS_DENIED",
                                "auth.access_denied"
                        ))
                        .jwt(jwt -> jwt
                                .decoder(jwtAccessDecoder)
                                .jwtAuthenticationConverter(authenticationConverter)
                        )
                )
                .build();
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            ObjectMapper objectMapper,
            ErrorResponseFactory errorResponseFactory,
            String code,
            @PropertyKey(resourceBundle = "i18n.messages") String messageKey
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                errorResponseFactory.create(request, code, messageKey)
        );
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
                    new X509EncodedKeySpec(KeyFileReader.readPem(jwtProperties.publicKeyLocation(), "PUBLIC KEY"))
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
                    new PKCS8EncodedKeySpec(KeyFileReader.readPem(jwtProperties.privateKeyLocation(), "PRIVATE KEY"))
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

}
