package com.rubp.whattoeat.server.feature.account.controller;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountRequest;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.account.service.RegisterService;
import com.rubp.whattoeat.server.feature.auth.config.PasswordConfig;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationService;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegisterController.class, properties = "spring.profiles.active=test")
@Import({
    RegisterService.class,
    InvitationService.class,
    JwtTokenService.class,
    SessionTokenService.class,
    PasswordConfig.class,
    SecurityConfig.class
})
public class RegisterControllerTest {

    private static final long ACCOUNT_ID = 123L;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String INVITATION_CODE = "AAECAwQFBgcICQoLDA0ODw";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("jwtAccessDecoder")
    private JwtDecoder accessDecoder;

    @Autowired
    @Qualifier("jwtRefreshDecoder")
    private JwtDecoder refreshDecoder;

    @MockitoBean
    private AccountRepository repository;

    @MockitoBean
    private InvitationRepository invitationRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }



    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.now(), ZoneOffset.UTC);
        }
    }
}
