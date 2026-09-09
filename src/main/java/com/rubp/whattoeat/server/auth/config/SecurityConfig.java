package com.rubp.whattoeat.server.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            @Qualifier("jwtAccessDecoder") JwtDecoder jwtAccessDecoder
    ) {
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
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/anonymous",
                                "/auth/refresh",
                                "/auth/logout"
                        ).permitAll().anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.decoder(jwtAccessDecoder))
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

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(),
                        new JwtClaimValidator<String>(
                                "token_type",
                                tokenType::equals
                        )
                )
        );

        return decoder;
    }

    private ECPublicKey publicKey(JwtProperties jwtProperties){

        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "无法创建EC密匙的factory",
                    exception);
        }

        Base64.Decoder decoder = Base64.getDecoder();

        try{
            return (ECPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decoder.decode(jwtProperties.publicKeyBase64()))
            );

        } catch (InvalidKeySpecException | IllegalArgumentException exception){
            throw new IllegalStateException(
                    "无法获取EC签名密钥",
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
                    "无法创建EC密匙的factory",
                    exception);
        }

        Base64.Decoder decoder = Base64.getDecoder();

        try{
            return (ECPrivateKey) keyFactory.generatePublic(
                    new PKCS8EncodedKeySpec(decoder.decode(jwtProperties.privateKeyBase64()))
            );

        } catch (InvalidKeySpecException | IllegalArgumentException exception){
            throw new IllegalStateException(
                    "无法获取EC签名密钥",
                    exception
            );
        }
    }

    private ECKey verificationKey(JwtProperties jwtProperties){

        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "无法创建EC密匙的factory",
                    exception);
        }

        Base64.Decoder decoder = Base64.getDecoder();

        ECPublicKey publicKey;

        try{
            publicKey = (ECPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decoder.decode(jwtProperties.publicKeyBase64()))
            );

            return new ECKey.Builder(Curve.P_256, publicKey)
                    .algorithm(JWSAlgorithm.ES256)
                    .build();

        } catch (InvalidKeySpecException | IllegalArgumentException exception){
            throw new IllegalStateException(
                    "无法获取EC签名密钥",
                    exception
            );
        }

    }
}
