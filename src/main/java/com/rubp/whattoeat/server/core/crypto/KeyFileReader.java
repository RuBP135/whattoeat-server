package com.rubp.whattoeat.server.core.crypto;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class KeyFileReader {

    private KeyFileReader() {
    }

    public static byte[] readPem(Resource resource, String type) {
        String pem = readAscii(resource).strip();
        String beginMarker = "-----BEGIN " + type + "-----";
        String endMarker = "-----END " + type + "-----";

        if (pem.length() < beginMarker.length() + endMarker.length()
                || !pem.startsWith(beginMarker)
                || !pem.endsWith(endMarker)) {
            throw new IllegalStateException("Key file must use the expected PEM format: " + type);
        }

        String encodedKey = pem.substring(
                beginMarker.length(),
                pem.length() - endMarker.length()
        ).replaceAll("\\s", "");

        return decodeBase64(encodedKey);
    }

    public static byte[] readBase64(Resource resource) {
        return decodeBase64(readAscii(resource).strip());
    }

    private static String readAscii(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.US_ASCII);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to read key file. Check that it exists and is readable.",
                    exception
            );
        }
    }

    private static byte[] decodeBase64(String encodedKey) {
        try {
            return Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Key file contains invalid Base64 data", exception);
        }
    }
}
