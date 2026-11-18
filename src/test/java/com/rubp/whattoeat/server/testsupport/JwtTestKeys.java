package com.rubp.whattoeat.server.testsupport;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public record JwtTestKeys(Resource privateKey, Resource publicKey) {

    public static JwtTestKeys generatePemKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = generator.generateKeyPair();
        return new JwtTestKeys(
                pemResource(PemKeyType.PRIVATE_KEY, keyPair.getPrivate().getEncoded()),
                pemResource(PemKeyType.PUBLIC_KEY, keyPair.getPublic().getEncoded())
        );
    }

    private static Resource pemResource(PemKeyType type, byte[] encodedKey) {
        String pem = "-----BEGIN " + type.label + "-----\n"
                + Base64.getEncoder().encodeToString(encodedKey)
                + "\n-----END " + type.label + "-----";
        return new ByteArrayResource(pem.getBytes(StandardCharsets.US_ASCII));
    }

    private enum PemKeyType {
        PRIVATE_KEY("PRIVATE KEY"),
        PUBLIC_KEY("PUBLIC KEY");

        private final String label;

        PemKeyType(String label) {
            this.label = label;
        }
    }
}
