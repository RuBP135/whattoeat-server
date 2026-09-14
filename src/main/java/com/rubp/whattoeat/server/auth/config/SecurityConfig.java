package com.rubp.whattoeat.server.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.rubp.whattoeat.server.web.error.ErrorResponse;
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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
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
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            @Qualifier("jwtAccessDecoder") JwtDecoder jwtAccessDecoder,
            ObjectMapper objectMapper
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
                                "/api/auth/anonymous",
                                "/api/auth/refresh"
                        ).permitAll().anyRequest().authenticated()
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
                    "无法创建 EC 密钥工厂",
                    exception);
        }

        try{
            return (ECPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(readPem(jwtProperties.publicKeyLocation(), "PUBLIC KEY"))
            );

        } catch (InvalidKeySpecException exception){
            throw new IllegalStateException(
                    "无法解析 EC 公钥",
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
                    "无法创建 EC 密钥工厂",
                    exception);
        }

        try{
            return (ECPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(readPem(jwtProperties.privateKeyLocation(), "PRIVATE KEY"))
            );

        } catch (InvalidKeySpecException exception){
            throw new IllegalStateException(
                    "无法解析 EC 私钥",
                    exception
            );
        }
    }

    private ECKey verificationKey(JwtProperties jwtProperties){

        try{
            return new ECKey.Builder(Curve.P_256, publicKey(jwtProperties))
                    .algorithm(JWSAlgorithm.ES256)
                    .build();

        } catch (IllegalArgumentException exception){
            throw new IllegalStateException(
                    "无法获取 EC 签名验证公钥",
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
                    "无法打开密匙文件，请确认文件是否存在及访问权限",
                    exception
            );
        }

        String beginMarker = "-----BEGIN " + type + "-----";
        String endMarker = "-----END " + type + "-----";

        if(!pem.startsWith(beginMarker) || !pem.endsWith(endMarker)){
            throw new IllegalStateException(
                    "密匙文件不是预期的PEM格式"
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
                    "密钥文件包含无效的 Base64 内容",
                    exception
            );
        }
    }
}
