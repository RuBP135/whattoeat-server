package com.rubp.whattoeat.server.feature.account.domain;

import com.rubp.whattoeat.server.core.crypto.KeyFileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;

@Component
public class AccountEmailCrypto {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec hmacKey;
    private final SecureRandom random = new SecureRandom();

    public AccountEmailCrypto(
            @Value("${account.email.encryption-key-location}") Resource encryptionKeyLocation,
            @Value("${account.email.hmac-key-location}") Resource hmacKeyLocation
    ) {
        byte[] encryptionKeyBytes = readKey(encryptionKeyLocation, "encryption");
        byte[] hmacKeyBytes = readKey(hmacKeyLocation, "HMAC");
        if (Arrays.equals(encryptionKeyBytes, hmacKeyBytes)) {
            throw new IllegalStateException("Account email encryption and HMAC require distinct keys");
        }

        this.encryptionKey = new SecretKeySpec(encryptionKeyBytes, "AES");
        this.hmacKey = new SecretKeySpec(hmacKeyBytes, HMAC_ALGORITHM);
    }

    public byte[] encrypt(String email) {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        random.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertextWithTag = cipher.doFinal(email.strip().getBytes(StandardCharsets.UTF_8));

            return ByteBuffer.allocate(iv.length + ciphertextWithTag.length)
                    .put(iv)
                    .put(ciphertextWithTag)
                    .array();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to encrypt account email", exception);
        }
    }

    public byte[] hmac(String email) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);
            return mac.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to calculate account email HMAC", exception);
        }
    }

    private static byte[] readKey(Resource location, String purpose) {
        byte[] key;
        try {
            key = KeyFileReader.readBase64(location);
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("Failed to read account email " + purpose + " key", exception);
        }

        if (key.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException("Account email " + purpose + " key must contain 32 bytes");
        }
        return key;
    }
}
