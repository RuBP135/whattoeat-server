package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import com.rubp.whattoeat.server.core.crypto.RandomCodeGenerator;
import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailCoolDownRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailVerificationSessionEntry;
import com.rubp.whattoeat.server.feature.account.data.EmailVerificationSessionRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountEmailCrypto;
import com.rubp.whattoeat.server.feature.account.domain.EmailVerificationCodeGenerator;
import com.rubp.whattoeat.server.feature.account.exception.EmailAlreadyRegisteredException;
import com.rubp.whattoeat.server.feature.account.exception.EmailCooldownException;
import com.rubp.whattoeat.server.feature.account.exception.InvalidInvitationCodeException;
import com.rubp.whattoeat.server.feature.account.exception.InvalidVerificationCodeException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.account.model.StartEmailRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.StartRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.VerifyRegistrationRequest;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class RegisterService {

    private final Clock clock;
    private final AccountRepository accountRepository;
    private final InvitationRepository invitationRepository;
    private final EmailCoolDownRepository emailCoolDownRepository;
    private final AccountEmailCrypto emailCrypto;
    private final VerificationEmailSender verificationEmailSender;
    private final EmailVerificationSessionRepository emailVerificationSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;
    private final Duration verificationTtl;

    public RegisterService(
            Clock clock,
            AccountRepository accountRepository,
            InvitationRepository invitationRepository,
            EmailCoolDownRepository emailCoolDownRepository,
            AccountEmailCrypto emailCrypto,
            VerificationEmailSender verificationEmailSender,
            EmailVerificationSessionRepository emailVerificationSessionRepository,
            PasswordEncoder passwordEncoder,
            SessionTokenService sessionTokenService,
            @Value("${account.email.verification-ttl}") Duration verificationTtl
    ) {
        this.clock = clock;
        this.accountRepository = accountRepository;
        this.invitationRepository = invitationRepository;
        this.emailCoolDownRepository = emailCoolDownRepository;
        this.emailCrypto = emailCrypto;
        this.verificationEmailSender = verificationEmailSender;
        this.emailVerificationSessionRepository = emailVerificationSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
        this.verificationTtl = verificationTtl;
    }

    public StartRegistrationResponse startRegistration(StartEmailRegistrationRequest request) {
        InvitationEntry invitation = findValidInvitation(request.invitationCode());

        String email = request.email().strip();
        byte[] emailHmac = emailCrypto.hmac(email);
        if (!emailCoolDownRepository.tryStartCooldown(emailHmac)) {
            throw new EmailCooldownException();
        }

        byte[] sessionCode = RandomCodeGenerator.generate();
        String verificationCode = Integer.toString(EmailVerificationCodeGenerator.generate());
        byte[] emailCiphertext = emailCrypto.encrypt(email);
        byte[] verificationCodeHmac = emailCrypto.hmacVerificationCode(sessionCode, verificationCode);

        verificationEmailSender.sendVerificationCode(email, verificationCode);
        Instant expiresAt = Instant.now(clock).plus(verificationTtl);
        emailVerificationSessionRepository.save(new EmailVerificationSessionEntry(
                sessionCode,
                invitation,
                emailCiphertext,
                emailHmac,
                verificationCodeHmac,
                expiresAt
        ));
        return new StartRegistrationResponse(Base64UrlCodec.encode(sessionCode));
    }

    @Transactional
    public void verifyRegistration(VerifyRegistrationRequest request) {
        EmailVerificationSessionEntry session = findUnexpiredSession(request.sessionCode());
        byte[] verificationCodeHmac = emailCrypto.hmacVerificationCode(
                session.getSessionCode(), request.verificationCode()
        );
        if (!MessageDigest.isEqual(session.getVerificationCodeHmac(), verificationCodeHmac)) {
            throw new InvalidVerificationCodeException();
        }

        if (accountRepository.existsByEmailHmac(session.getEmailHmac())) {
            throw new EmailAlreadyRegisteredException();
        }

        session.markVerified();
    }

    @Transactional
    public CompleteRegistrationResponse completeRegistration(CompleteRegistrationRequest request) {
        EmailVerificationSessionEntry session = findUnexpiredSession(request.sessionCode());
        if (!session.isVerified()) {
            throw new InvalidVerificationCodeException();
        }

        byte[] invitationCode = session.getInvitation().getCode();
        AccountEntry account = new AccountEntry(
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now(clock)
        );
        account.setEmail(session.getEmailCiphertext(), session.getEmailHmac());

        if (invitationRepository.deleteValidInvitation(invitationCode, Instant.now(clock)) == 0) {
            throw new InvalidInvitationCodeException();
        }
        if (accountRepository.insertIfEmailAbsent(account) == 0) {
            throw new EmailAlreadyRegisteredException();
        }

        AccountEntry savedAccount = accountRepository.findByEmailHmac(account.getEmailHmac())
                .orElseThrow(() -> new IllegalStateException("Inserted account was not found"));
        var tokens = sessionTokenService.issueTokenPair(savedAccount);
        return new CompleteRegistrationResponse(tokens.accessToken(), tokens.refreshToken());
    }

    private EmailVerificationSessionEntry findUnexpiredSession(String encodedSessionCode) {
        byte[] sessionCode;
        try {
            sessionCode = Base64UrlCodec.decode(encodedSessionCode);
        } catch (IllegalArgumentException exception) {
            throw new InvalidVerificationCodeException();
        }

        EmailVerificationSessionEntry session = emailVerificationSessionRepository.findById(sessionCode)
                .orElseThrow(InvalidVerificationCodeException::new);
        if (!session.getRegistrationExpiresAt().isAfter(Instant.now(clock))) {
            throw new InvalidVerificationCodeException();
        }
        return session;
    }

    private InvitationEntry findValidInvitation(String invitationCode) {
        byte[] code;
        try {
            code = Base64UrlCodec.decode(invitationCode);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInvitationCodeException();
        }
        return invitationRepository.findByCodeAndExpiresAtAfter(code, Instant.now(clock))
                .orElseThrow(InvalidInvitationCodeException::new);
    }

}
