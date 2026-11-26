package com.rubp.whattoeat.server.feature.account.controller;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import com.rubp.whattoeat.server.core.config.PasswordConfig;
import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailCoolDownRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailRegistrationSessionEntry;
import com.rubp.whattoeat.server.feature.account.data.EmailRegistrationSessionRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountEmailCrypto;
import com.rubp.whattoeat.server.feature.account.domain.AccountStatus;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.StartEmailRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.VerifyRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.service.RegisterService;
import com.rubp.whattoeat.server.feature.account.service.VerificationEmailSender;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationAdminService;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegisterController.class, properties = "spring.profiles.active=test")
@Import({
    RegisterService.class,
    InvitationAdminService.class,
    JwtTokenService.class,
    SessionTokenService.class,
    PasswordConfig.class,
    SecurityConfig.class
})
public class RegisterControllerTest {

    private static final long ACCOUNT_ID = 123L;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "user@example.com";
    private static final String INVITATION_CODE = "ABCDEfGHIjkLMNoPQRST0w";
    private static final String SESSION_CODE = "aBCdEfGhIjkLMNoPQRsT1w";
    private static final String VERIFICATION_CODE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountEmailCrypto emailCrypto;

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
    private InvitationRepository invitationRepository;

    @MockitoBean
    private EmailCoolDownRepository emailCoolDownRepository;

    @MockitoBean
    private EmailRegistrationSessionRepository emailRegistrationSessionRepository;

    @MockitoBean
    private VerificationEmailSender verificationEmailSender;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }

    @Test
    public void startRegistration_shouldSendCodeAndReturnSessionCode() throws Exception {
        InvitationEntry invitation = validInvitation();
        byte[] emailHmac = emailCrypto.hmac(EMAIL);
        when(invitationRepository.findByCodeAndExpiresAtAfter(invitation.getCode(), Instant.now(clock)))
                .thenReturn(Optional.of(invitation));
        when(emailCoolDownRepository.tryStartCooldown(emailHmac)).thenReturn(true);

        var result = register("/start/email", new StartEmailRegistrationRequest(INVITATION_CODE, EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        var codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(verificationEmailSender).sendVerificationCode(eq(EMAIL), codeCaptor.capture());
        String sentCode = codeCaptor.getValue();
        assertTrue(sentCode.matches("[0-9]{6}"));

        var sessionCaptor = ArgumentCaptor.forClass(EmailRegistrationSessionEntry.class);
        verify(emailRegistrationSessionRepository).save(sessionCaptor.capture());
        EmailRegistrationSessionEntry session = sessionCaptor.getValue();
        result.andExpect(jsonPath("$.sessionCode").value(Base64UrlCodec.encode(session.getSessionCode())));
        assertArrayEquals(emailHmac, session.getEmailHmac());
        assertArrayEquals(
                emailCrypto.hmacVerificationCode(session.getSessionCode(), sentCode),
                session.getVerificationCodeHmac()
        );
    }

    @Test
    public void startRegistration_shouldRejectInvalidInvitationCode() throws Exception {
        byte[] invitationCode = Base64UrlCodec.decode(INVITATION_CODE);
        when(invitationRepository.findByCodeAndExpiresAtAfter(invitationCode, Instant.now(clock)))
                .thenReturn(Optional.empty());

        register("/start/email", new StartEmailRegistrationRequest(INVITATION_CODE, EMAIL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INVITATION_CODE"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verifyNoInteractions(emailCoolDownRepository, verificationEmailSender, emailRegistrationSessionRepository);
    }

    @Test
    public void startRegistration_shouldRejectEmailDuringCooldown() throws Exception {
        InvitationEntry invitation = validInvitation();
        when(invitationRepository.findByCodeAndExpiresAtAfter(invitation.getCode(), Instant.now(clock)))
                .thenReturn(Optional.of(invitation));
        when(emailCoolDownRepository.tryStartCooldown(emailCrypto.hmac(EMAIL))).thenReturn(false);

        register("/start/email", new StartEmailRegistrationRequest(INVITATION_CODE, EMAIL))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_COOLDOWN"));
        verifyNoInteractions(verificationEmailSender, emailRegistrationSessionRepository);
    }

    @Test
    public void verifyRegistration_shouldMarkSessionVerified() throws Exception {
        EmailRegistrationSessionEntry session = givenUnverifiedSession();
        when(accountRepository.existsByEmailHmac(session.getEmailHmac())).thenReturn(false);

        register("/verify/email", new VerifyRegistrationRequest(SESSION_CODE, VERIFICATION_CODE))
                .andExpect(status().isNoContent());

        assertTrue(session.isVerified());
    }

    @Test
    public void verifyRegistration_shouldRejectIncorrectVerificationCode() throws Exception {
        EmailRegistrationSessionEntry session = givenUnverifiedSession();

        register("/verify/email", new VerifyRegistrationRequest(SESSION_CODE, "654321"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_CODE"));
        assertFalse(session.isVerified());
        verifyNoInteractions(accountRepository);
    }

    @Test
    public void verifyRegistration_shouldRejectAlreadyRegisteredEmail() throws Exception {
        EmailRegistrationSessionEntry session = givenUnverifiedSession();
        when(accountRepository.existsByEmailHmac(session.getEmailHmac())).thenReturn(true);

        register("/verify/email", new VerifyRegistrationRequest(SESSION_CODE, VERIFICATION_CODE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertFalse(session.isVerified());
    }

    @Test
    public void verifyRegistration_shouldRejectExpiredSession() throws Exception {
        EmailRegistrationSessionEntry session = givenUnverifiedSession(Instant.now(clock).minusSeconds(1));

        register("/verify/email", new VerifyRegistrationRequest(SESSION_CODE, VERIFICATION_CODE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REGISTRATION_SESSION"))
                .andExpect(jsonPath("$.message").value("注册会话无效或已过期"));

        assertFalse(session.isVerified());
        verifyNoInteractions(accountRepository);
    }

    @Test
    public void completeRegistration_shouldCreateAccountAndReturnSignedTokens() throws Exception {
        EmailRegistrationSessionEntry session = givenUnverifiedSession();
        session.markVerified();
        byte[] invitationCode = session.getInvitation().getCode();
        AccountEntry savedAccount = new AccountEntry(
                USERNAME, passwordEncoder.encode(PASSWORD), Role.USER, AccountStatus.ACTIVE, Instant.now(clock)
        );
        savedAccount.setEmail(session.getEmailCiphertext(), session.getEmailHmac());
        ReflectionTestUtils.setField(savedAccount, "id", ACCOUNT_ID);
        when(invitationRepository.deleteValidInvitation(invitationCode, Instant.now(clock))).thenReturn(1);
        when(accountRepository.insertIfEmailAbsent(any(AccountEntry.class))).thenReturn(1);
        when(accountRepository.findByEmailHmac(session.getEmailHmac())).thenReturn(Optional.of(savedAccount));

        var result = register("/complete/email", new CompleteRegistrationRequest(SESSION_CODE, USERNAME, PASSWORD))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        CompleteRegistrationResponse response = mapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                CompleteRegistrationResponse.class
        );
        assertEquals(Long.toString(ACCOUNT_ID), accessDecoder.decode(response.accessToken()).getSubject());
        assertEquals(Long.toString(ACCOUNT_ID), refreshDecoder.decode(response.refreshToken()).getSubject());
        verify(invitationRepository).deleteValidInvitation(invitationCode, Instant.now(clock));
        verify(refreshTokenRepository).save(any(RefreshTokenEntry.class));

        var accountCaptor = ArgumentCaptor.forClass(AccountEntry.class);
        verify(accountRepository).insertIfEmailAbsent(accountCaptor.capture());
        AccountEntry account = accountCaptor.getValue();
        assertEquals(USERNAME, account.getUsername());
        assertTrue(passwordEncoder.matches(PASSWORD, account.getPasswordHash()));
        assertArrayEquals(session.getEmailCiphertext(), account.getEmailCiphertext());
        assertArrayEquals(session.getEmailHmac(), account.getEmailHmac());
    }

    @Test
    public void completeRegistration_shouldRejectUsernameContainingAtSign() throws Exception {
        register("/complete/email", new CompleteRegistrationRequest(SESSION_CODE, USERNAME + "@", PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(emailRegistrationSessionRepository, accountRepository);
    }

    @Test
    public void completeRegistration_shouldRejectUnknownSessionCode() throws Exception {
        byte[] sessionCode = Base64UrlCodec.decode(SESSION_CODE);
        when(emailRegistrationSessionRepository.findById(sessionCode)).thenReturn(Optional.empty());

        assertCompleteRegistrationRejected("INVALID_REGISTRATION_SESSION", "注册会话无效或已过期");

        verify(emailRegistrationSessionRepository).findById(sessionCode);
    }

    @Test
    public void completeRegistration_shouldRejectUnverifiedSession() throws Exception {
        givenUnverifiedSession();

        assertCompleteRegistrationRejected("INVALID_VERIFICATION_CODE", "验证码无效或已过期");
    }

    private void assertCompleteRegistrationRejected(String errorCode, String message) throws Exception {
        register("/complete/email", new CompleteRegistrationRequest(SESSION_CODE, USERNAME, PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(errorCode))
                .andExpect(jsonPath("$.message").value(message));

        verifyNoInteractions(invitationRepository, accountRepository, refreshTokenRepository);
    }

    private ResultActions register(String path, Object request) throws Exception {
        return mockMvc.perform(post("/api/accounts/register" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));
    }

    private InvitationEntry validInvitation() {
        return new InvitationEntry(
                Base64UrlCodec.decode(INVITATION_CODE), Instant.now(clock).plus(Duration.ofDays(1))
        );
    }

    private EmailRegistrationSessionEntry givenUnverifiedSession() {
        return givenUnverifiedSession(Instant.now(clock).plus(Duration.ofMinutes(5)));
    }

    private EmailRegistrationSessionEntry givenUnverifiedSession(Instant expiresAt) {
        byte[] sessionCode = Base64UrlCodec.decode(SESSION_CODE);
        var session = new EmailRegistrationSessionEntry(
                sessionCode,
                validInvitation(),
                emailCrypto.encrypt(EMAIL),
                emailCrypto.hmac(EMAIL),
                emailCrypto.hmacVerificationCode(sessionCode, VERIFICATION_CODE),
                expiresAt
        );
        when(emailRegistrationSessionRepository.findById(sessionCode)).thenReturn(Optional.of(session));
        return session;
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.now(), ZoneOffset.UTC);
        }

        @Bean
        AccountEmailCrypto emailCrypto() {
            byte[] encryptionKey = new byte[32];
            byte[] hmacKey = new byte[32];
            hmacKey[0] = 1;
            return new AccountEmailCrypto(
                    new ByteArrayResource(Base64.getEncoder().encode(encryptionKey)),
                    new ByteArrayResource(Base64.getEncoder().encode(hmacKey))
            );
        }
    }
}
