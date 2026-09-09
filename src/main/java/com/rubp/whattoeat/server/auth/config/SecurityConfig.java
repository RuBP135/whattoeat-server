package com.rubp.whattoeat.server.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

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
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {

        ECKey key = signingKey(jwtProperties);
        JWKSet keySet = new JWKSet(key);

        JWKSource<SecurityContext> jwkSource =
                (selector, context) ->
                        selector.select(keySet);

        return new NimbusJwtEncoder(jwkSource);
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



    private ECKey signingKey(JwtProperties jwtProperties){

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
        ECPrivateKey privateKey;

        try{
            publicKey = (ECPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decoder.decode(jwtProperties.publicKeyBase64()))
            );

            privateKey = (ECPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decoder.decode(jwtProperties.privateKeyBase64()))
            );

            return new ECKey.Builder(Curve.P_256, publicKey)
                    .privateKey(privateKey)
                    .algorithm(JWSAlgorithm.ES256)
                    .build();

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
