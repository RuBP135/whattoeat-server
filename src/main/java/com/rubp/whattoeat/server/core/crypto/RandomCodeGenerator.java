package com.rubp.whattoeat.server.core.crypto;

import java.security.SecureRandom;

public final class RandomCodeGenerator {

    private static final int CODE_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomCodeGenerator() {
    }

    public static byte[] generate() {
        byte[] code = new byte[CODE_LENGTH_BYTES];
        RANDOM.nextBytes(code);
        return code;
    }
}
