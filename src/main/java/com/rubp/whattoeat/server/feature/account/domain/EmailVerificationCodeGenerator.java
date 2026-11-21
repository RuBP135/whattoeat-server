package com.rubp.whattoeat.server.feature.account.domain;

import java.security.SecureRandom;

public final class EmailVerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private EmailVerificationCodeGenerator() {
    }

    public static int generate() {
        return RANDOM.nextInt(100_000, 1_000_000);
    }
}
