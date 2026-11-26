package com.rubp.whattoeat.server.feature.invitation.service;

import com.rubp.whattoeat.server.core.codec.Base64UrlCodec;
import com.rubp.whattoeat.server.core.crypto.RandomCodeGenerator;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import com.rubp.whattoeat.server.feature.invitation.exception.InvalidInvitationCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class InvitationAdminService {

    private static final Logger log = LoggerFactory.getLogger(InvitationAdminService.class);

    private final InvitationRepository invitationRepository;
    private final Clock clock;
    private final Duration ttl;

    public InvitationAdminService(
            InvitationRepository invitationRepository,
            Clock clock,
            @Value("${invitation.ttl}") Duration ttl
    ) {
        if(ttl == null || !ttl.isPositive()){
            throw new IllegalArgumentException("Invitation code TTL must be greater than zero");
        }

        this.invitationRepository = invitationRepository;
        this.clock = clock;
        this.ttl = ttl;
    }

    public String createInvitation() {
        Instant expiresAt = Instant.now(clock).plus(ttl);
        byte[] code = RandomCodeGenerator.generate();

        invitationRepository.save(new InvitationEntry(
                code,
                expiresAt
        ));

        return Base64UrlCodec.encode(code);
    }

    public InvitationEntry findValidInvitation(String invitationCode) {
        byte[] code;
        try {
            code = Base64UrlCodec.decode(invitationCode);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInvitationCodeException();
        }
        return invitationRepository.findByCodeAndExpiresAtAfter(code, Instant.now(clock))
                .orElseThrow(InvalidInvitationCodeException::new);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean consumeInvitation(byte[] invitationCode) {
        return invitationRepository.deleteValidInvitation(invitationCode, Instant.now(clock)) == 1;
    }

    @Scheduled(fixedDelayString = "${invitation.cleanup-interval}")
    @Transactional
    public void cleanupExpiredInvitations() {
        int deletedCount = invitationRepository.deleteExpiredInvitations(Instant.now(clock));
        if (deletedCount > 0) {
            log.info("Deleted {} expired invitation codes", deletedCount);
        }
    }
}
