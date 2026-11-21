package com.rubp.whattoeat.server.feature.auth.controller;

import com.rubp.whattoeat.server.core.config.PasswordConfig;
import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountEmailCrypto;
import com.rubp.whattoeat.server.feature.account.domain.AccountStatus;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.model.LoginRequest;
import com.rubp.whattoeat.server.feature.auth.model.LoginResponse;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.auth.service.LoginService;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class, properties = "spring.profiles.active=test")
@Import({
    LoginService.class,
    JwtTokenService.class,
    SessionTokenService.class,
    PasswordConfig.class,
    SecurityConfig.class
})
public class LoginControllerTest {

    private static final long ACCOUNT_ID = 123L;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("jwtAccessDecoder")
    private JwtDecoder accessDecoder;

    @Autowired
    @Qualifier("jwtRefreshDecoder")
    private JwtDecoder refreshDecoder;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private AccountEmailCrypto emailCrypto;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }

    @Test
    public void login_shouldReturnSignedTokensUsingUsername() throws Exception {
        AccountEntry account = givenActiveAccount();

        var result = login(USERNAME, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        LoginResponse response = mapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                LoginResponse.class
        );
        var accessJwt = accessDecoder.decode(response.accessToken());
        var refreshJwt = refreshDecoder.decode(response.refreshToken());
        assertEquals(Long.toString(ACCOUNT_ID), accessJwt.getSubject());
        assertEquals(Long.toString(ACCOUNT_ID), refreshJwt.getSubject());

        var refreshTokenCaptor = ArgumentCaptor.forClass(RefreshTokenEntry.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshTokenEntry savedToken = refreshTokenCaptor.getValue();
        assertEquals(account, savedToken.getAccount());
        assertEquals(jwtTokenService.hashRefreshToken(response.refreshToken()), savedToken.getRefreshTokenHash());
        assertEquals(refreshJwt.getExpiresAt(), savedToken.getExpiresAt());

        verify(accountRepository).findByUsername(USERNAME);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(emailCrypto);
    }

    @Test
    public void login_shouldReturnTokensUsingEmail() throws Exception {
        String email = "user@example.com";
        byte[] emailHmac = new byte[32];
        AccountEntry account = createActiveAccount();
        when(emailCrypto.hmac(email)).thenReturn(emailHmac);
        when(accountRepository.findByEmailHmac(emailHmac)).thenReturn(Optional.of(account));

        login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        verify(emailCrypto).hmac(email);
        verify(accountRepository).findByEmailHmac(emailHmac);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void login_shouldRejectUnknownUsername() throws Exception {
        String unknownUsername = "unknown-user";
        when(accountRepository.findByUsername(unknownUsername)).thenReturn(Optional.empty());

        assertInvalidCredentials(unknownUsername, PASSWORD);

        verify(accountRepository).findByUsername(unknownUsername);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    public void login_shouldRejectIncorrectPassword() throws Exception {
        givenActiveAccount();

        assertInvalidCredentials(USERNAME, "wrong-password");

        verify(accountRepository).findByUsername(USERNAME);
        verifyNoInteractions(refreshTokenRepository);
    }

    private AccountEntry givenActiveAccount() {
        AccountEntry account = createActiveAccount();
        when(accountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(account));
        return account;
    }

    private AccountEntry createActiveAccount() {
        AccountEntry account = new AccountEntry(
                USERNAME, passwordEncoder.encode(PASSWORD), Role.USER, AccountStatus.ACTIVE, Instant.now(clock)
        );
        ReflectionTestUtils.setField(account, "id", ACCOUNT_ID);
        return account;
    }

    private ResultActions login(String identifier, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new LoginRequest(identifier, password))));
    }

    private void assertInvalidCredentials(String username, String password) throws Exception {
        login(username, password)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
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
