package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import com.rubp.whattoeat.server.core.crypto.RandomCodeGenerator;
import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailCoolDownRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailRegistrationSessionEntry;
import com.rubp.whattoeat.server.feature.account.data.EmailRegistrationSessionRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountEmailCrypto;
import com.rubp.whattoeat.server.feature.account.domain.EmailVerificationCodeGenerator;
import com.rubp.whattoeat.server.feature.account.exception.EmailAlreadyRegisteredException;
import com.rubp.whattoeat.server.feature.account.exception.EmailCooldownException;
import com.rubp.whattoeat.server.feature.account.exception.InvalidRegistrationSessionException;
import com.rubp.whattoeat.server.feature.account.exception.InvalidVerificationCodeException;
import com.rubp.whattoeat.server.feature.account.domain.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.CompleteRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.account.model.StartEmailRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.StartRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.VerifyRegistrationRequest;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.exception.InvalidInvitationCodeException;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationAdminService;
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
    private final InvitationAdminService invitationAdminService;
    private final EmailCoolDownRepository emailCoolDownRepository;
    private final AccountEmailCrypto emailCrypto;
    private final VerificationEmailSender verificationEmailSender;
    private final EmailRegistrationSessionRepository emailRegistrationSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;
    private final Duration verificationTtl;

    public RegisterService(
            Clock clock,
            AccountRepository accountRepository,
            InvitationAdminService invitationAdminService,
            EmailCoolDownRepository emailCoolDownRepository,
            AccountEmailCrypto emailCrypto,
            VerificationEmailSender verificationEmailSender,
            EmailRegistrationSessionRepository emailRegistrationSessionRepository,
            PasswordEncoder passwordEncoder,
            SessionTokenService sessionTokenService,
            @Value("${account.email.verification-ttl}") Duration verificationTtl
    ) {
        this.clock = clock;
        this.accountRepository = accountRepository;
        this.invitationAdminService = invitationAdminService;
        this.emailCoolDownRepository = emailCoolDownRepository;
        this.emailCrypto = emailCrypto;
        this.verificationEmailSender = verificationEmailSender;
        this.emailRegistrationSessionRepository = emailRegistrationSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
        this.verificationTtl = verificationTtl;
    }

    public StartRegistrationResponse startRegistration(StartEmailRegistrationRequest request) {
        InvitationEntry invitation = invitationAdminService.findValidInvitation(request.invitationCode());

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
        emailRegistrationSessionRepository.save(new EmailRegistrationSessionEntry(
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
        EmailRegistrationSessionEntry session = findValidSession(request.sessionCode());
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
        EmailRegistrationSessionEntry session = findValidSession(request.sessionCode());
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

        if (!invitationAdminService.consumeInvitation(invitationCode)) {
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

    private EmailRegistrationSessionEntry findValidSession(String sessionCode) {
        byte[] code;
        try {
            code = Base64UrlCodec.decode(sessionCode);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRegistrationSessionException();
        }

        EmailRegistrationSessionEntry session = emailRegistrationSessionRepository.findById(code)
                .orElseThrow(InvalidRegistrationSessionException::new);
        if (!session.getRegistrationExpiresAt().isAfter(Instant.now(clock))) {
            throw new InvalidRegistrationSessionException();
        }
        return session;
    }

}
