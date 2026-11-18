package com.rubp.whattoeat.server.feature.invitation.domain;

import java.security.SecureRandom;
import java.util.Base64;

public class InvitationCodeGenerator {

    private static final int CODE_BYTE_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] code = new byte[CODE_BYTE_LENGTH];
        secureRandom.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }
}
