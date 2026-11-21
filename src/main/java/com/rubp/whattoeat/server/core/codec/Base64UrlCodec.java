package com.rubp.whattoeat.server.core.codec;

import java.util.Base64;
import java.util.Objects;

public class Base64UrlCodec {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    private Base64UrlCodec() {}

    public static String encode(byte[] code) {
        return encoder.withoutPadding().encodeToString(code);
    }

    public static byte[] decode(String code) {
        byte[] decoded = decoder.decode(code);
        if(!Objects.equals(encode(decoded), code)) {
            throw new IllegalArgumentException("Non-canonical Base64URL encoding");
        }
        return decoder.decode(code);
    }
}
