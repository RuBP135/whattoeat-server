package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import com.rubp.whattoeat.server.core.crypto.RandomCodeGenerator;
import com.rubp.whattoeat.server.feature.account.data.EmailCoolDownRepository;
import com.rubp.whattoeat.server.feature.account.data.EmailVerificationSessionEntry;
import com.rubp.whattoeat.server.feature.account.data.EmailVerificationSessionRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountEmailCrypto;
import com.rubp.whattoeat.server.feature.account.domain.EmailVerificationCodeGenerator;
import com.rubp.whattoeat.server.feature.account.exception.EmailCooldownException;
import com.rubp.whattoeat.server.feature.account.exception.InvalidInvitationCodeException;
import com.rubp.whattoeat.server.feature.account.model.StartRegistrationRequest;
import com.rubp.whattoeat.server.feature.account.model.StartRegistrationResponse;
import com.rubp.whattoeat.server.feature.account.model.VerificationType;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class RegisterService {

    private final Clock clock;
    private final InvitationRepository invitationRepository;
    private final EmailCoolDownRepository emailCoolDownRepository;
    private final AccountEmailCrypto emailCrypto;
    private final VerificationEmailSender verificationEmailSender;
    private final EmailVerificationSessionRepository emailVerificationSessionRepository;
    private final Duration verificationTtl;

    public RegisterService(
            Clock clock,
            InvitationRepository invitationRepository,
            EmailCoolDownRepository emailCoolDownRepository,
            AccountEmailCrypto emailCrypto,
            VerificationEmailSender verificationEmailSender,
            EmailVerificationSessionRepository emailVerificationSessionRepository,
            @Value("${account.email.verification-ttl}") Duration verificationTtl
    ) {
        this.clock = clock;
        this.invitationRepository = invitationRepository;
        this.emailCoolDownRepository = emailCoolDownRepository;
        this.emailCrypto = emailCrypto;
        this.verificationEmailSender = verificationEmailSender;
        this.emailVerificationSessionRepository = emailVerificationSessionRepository;
        this.verificationTtl = verificationTtl;
    }

    public StartRegistrationResponse startRegistration(StartRegistrationRequest request) {
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
        return new StartRegistrationResponse(VerificationType.EMAIL, Base64UrlCodec.encode(sessionCode));
    }

    private InvitationEntry findValidInvitation(String invitationCode) {
        if (invitationCode == null) {
            throw new InvalidInvitationCodeException();
        }

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
