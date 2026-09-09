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
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

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

    private static final Base64.Decoder decoder = Base64.getDecoder();

    private static final KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "无法创建EC密匙的factory",
                    exception);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) throws InvalidKeySpecException {

        ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(decoder.decode(jwtProperties.publicKeyBase64()))
        );
        ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(decoder.decode(jwtProperties.privateKeyBase64()))
        );

        ECKey signingKey = new ECKey.Builder(Curve.P_256, publicKey)
                .privateKey(privateKey)
                .algorithm(JWSAlgorithm.ES256)
                .build();

        JWKSource<SecurityContext> jwkSource =
                (selector, context) ->
                        selector.select(new JWKSet(signingKey));

        return new NimbusJwtEncoder(jwkSource);
    }


}
