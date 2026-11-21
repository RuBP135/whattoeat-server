package com.rubp.whattoeat.server.core.crypto;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomCodeGeneratorTest {

    @Test
    public void generate_shouldReturn128BitRandomValueForUrlSafeBase64Encoding() {
        for(int attempt = 0; attempt < 100; ++attempt){
            String code = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(RandomCodeGenerator.generate());

            assertTrue(code.matches("[A-Za-z0-9_-]{22}"));
            assertEquals(16, Base64.getUrlDecoder().decode(code).length);
        }
    }
}
