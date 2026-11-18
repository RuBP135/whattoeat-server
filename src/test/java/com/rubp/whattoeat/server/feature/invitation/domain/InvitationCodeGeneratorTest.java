package com.rubp.whattoeat.server.feature.invitation.domain;

import com.rubp.whattoeat.server.feature.invitation.domain.InvitationCodeGenerator;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvitationCodeGeneratorTest {

    @Test
    public void generate_shouldReturnUrlSafeBase64CodeFor128BitRandomValue() {
        InvitationCodeGenerator generator = new InvitationCodeGenerator();

        for(int attempt = 0; attempt < 100; ++attempt){
            String code = generator.generate();

            assertTrue(code.matches("[A-Za-z0-9_-]{22}"));
            assertEquals(16, Base64.getUrlDecoder().decode(code).length);
        }
    }
}
