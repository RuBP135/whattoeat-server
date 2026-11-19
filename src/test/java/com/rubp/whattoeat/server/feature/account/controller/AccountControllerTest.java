package com.rubp.whattoeat.server.feature.account.controller;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountRequest;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.account.service.AccountService;
import com.rubp.whattoeat.server.feature.auth.config.PasswordConfig;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationService;
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

@WebMvcTest(controllers = AccountController.class, properties = "spring.profiles.active=test")
@Import({
    AccountService.class,
    InvitationService.class,
    JwtTokenService.class,
    SessionTokenService.class,
    PasswordConfig.class,
    SecurityConfig.class
})
public class AccountControllerTest {

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

    @Test
    public void createAccount_shouldSaveAccountAndReturnSignedTokensWithoutAuthentication() throws Exception {

        byte[] code = Base64.getUrlDecoder().decode(INVITATION_CODE);
        when(invitationRepository.deleteValidInvitation(code, Instant.now(clock))).thenReturn(1);
        when(repository.save(any(AccountEntry.class))).thenAnswer(invocation -> {
            AccountEntry account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", ACCOUNT_ID);
            return account;
        });

        var result = createAccount(INVITATION_CODE)
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        var accountCaptor = ArgumentCaptor.forClass(AccountEntry.class);
        verify(repository).save(accountCaptor.capture());
        AccountEntry savedAccount = accountCaptor.getValue();
        assertEquals(USERNAME, savedAccount.getUsername());
        assertEquals(Role.USER, savedAccount.getRole());
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertEquals(Instant.now(clock), savedAccount.getCreatedAt());
        assertTrue(savedAccount.getPasswordHash().startsWith("$argon2id$"));
        assertTrue(passwordEncoder.matches(PASSWORD, savedAccount.getPasswordHash()));

        CreateAccountResponse response = mapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                CreateAccountResponse.class
        );
        var accessJwt = accessDecoder.decode(response.accessToken());
        var refreshJwt = refreshDecoder.decode(response.refreshToken());
        assertEquals(Long.toString(ACCOUNT_ID), accessJwt.getSubject());
        assertEquals(Long.toString(ACCOUNT_ID), refreshJwt.getSubject());
        assertEquals("ACCESS", accessJwt.getClaimAsString("token_type"));
        assertEquals("REFRESH", refreshJwt.getClaimAsString("token_type"));
        assertEquals("ES256", accessJwt.getHeaders().get("alg"));
        assertEquals("ES256", refreshJwt.getHeaders().get("alg"));
        assertThrows(JwtException.class, () -> accessDecoder.decode(response.refreshToken()));
        assertThrows(JwtException.class, () -> refreshDecoder.decode(response.accessToken()));
    }

    @Test
    public void createAccount_shouldRejectUnknownInvitationCode() throws Exception {
        String unknownInvitationCode = "AAAAAAAAAAAAAAAAAAAAAA";
        byte[] code = Base64.getUrlDecoder().decode(unknownInvitationCode);
        Instant now = Instant.now(clock);
        when(invitationRepository.deleteValidInvitation(code, now)).thenReturn(0);

        createAccount(unknownInvitationCode)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INVITATION_CODE"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(invitationRepository).deleteValidInvitation(code, now);
        verifyNoInteractions(repository, refreshTokenRepository);
    }

    @Test
    public void createAccount_shouldRejectInvalidAccessToken() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"))
                .andExpect(jsonPath("$.message").isNotEmpty());
        verifyNoInteractions(repository);
    }

    private ResultActions createAccount(String invitationCode) throws Exception {
        return mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateAccountRequest(USERNAME, PASSWORD, invitationCode))));
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
