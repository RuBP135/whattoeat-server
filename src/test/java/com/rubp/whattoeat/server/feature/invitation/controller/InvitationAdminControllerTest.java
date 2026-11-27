package com.rubp.whattoeat.server.feature.invitation.controller;

import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationAdminService;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvitationAdminController.class, properties = "spring.profiles.active=test")
@Import({JwtTokenService.class, SecurityConfig.class})
public class InvitationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private InvitationAdminService invitationAdminService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }

    @Test
    public void createInvitation_shouldReturnCodeForAdmin() throws Exception {
        when(invitationAdminService.createInvitation()).thenReturn("Ab12Cd34Ef56Gh78Ij90Kl");

        createInvitation(Role.ADMIN)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.invitationCode").value("Ab12Cd34Ef56Gh78Ij90Kl"));

        verify(invitationAdminService).createInvitation();
    }

    @Test
    public void createInvitation_shouldRejectRegularUser() throws Exception {
        createInvitation(Role.USER)
                .andExpect(status().isForbidden());

        verifyNoInteractions(invitationAdminService);
    }

    private ResultActions createInvitation(Role role) throws Exception {
        String accessToken = jwtTokenService.createToken(123L, role, JwtTokenType.ACCESS);
        return mockMvc.perform(post("/api/admin/invitations")
                .header("Authorization", "Bearer " + accessToken));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.now(), ZoneOffset.UTC);
        }
    }
}
