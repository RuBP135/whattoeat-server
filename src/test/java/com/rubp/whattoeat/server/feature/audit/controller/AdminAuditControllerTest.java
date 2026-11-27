package com.rubp.whattoeat.server.feature.audit.controller;

import com.rubp.whattoeat.server.core.model.PageResponse;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation;
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType;
import com.rubp.whattoeat.server.feature.audit.model.AdminAuditLogResponse;
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService;
import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAuditController.class, properties = "spring.profiles.active=test")
@Import({JwtTokenService.class, SecurityConfig.class})
public class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private AdminAuditService adminAuditService;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }

    @Test
    public void listAuditLogs_shouldUseDefaultPaginationAndReturnPageResponseForAdmin() throws Exception {
        Instant createdAt = Instant.parse("2026-11-26T12:00:00Z");
        AdminAuditLogResponse item = new AdminAuditLogResponse(
                99L,
                7L,
                "admin",
                AuditOperation.CREATE,
                AuditResourceType.STALL,
                12L,
                "新建档口「川味面馆」（地点：一食堂，铺位：3）",
                createdAt
        );
        when(adminAuditService.listAuditLogs(null, null, 0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1L));

        performList(Role.ADMIN, get("/api/admin/audits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(99))
                .andExpect(jsonPath("$.items[0].adminId").value(7))
                .andExpect(jsonPath("$.items[0].adminName").value("admin"))
                .andExpect(jsonPath("$.items[0].operation").value("CREATE"))
                .andExpect(jsonPath("$.items[0].resourceType").value("STALL"))
                .andExpect(jsonPath("$.items[0].resourceId").value(12))
                .andExpect(jsonPath("$.items[0].summary").value("新建档口「川味面馆」（地点：一食堂，铺位：3）"))
                .andExpect(jsonPath("$.items[0].createdAt").value("2026-11-26T12:00:00Z"));

        verify(adminAuditService).listAuditLogs(null, null, 0, 20);
    }

    @Test
    public void listAuditLogs_shouldForwardFilterAndPaginationParams() throws Exception {
        when(adminAuditService.listAuditLogs(AuditOperation.CREATE, AuditResourceType.STALL, 1, 50))
                .thenReturn(new PageResponse<>(List.of(), 1, 50, 0L));

        performList(
                Role.ADMIN,
                get("/api/admin/audits")
                        .param("operation", "CREATE")
                        .param("resourceType", "STALL")
                        .param("page", "1")
                        .param("size", "50")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(adminAuditService).listAuditLogs(AuditOperation.CREATE, AuditResourceType.STALL, 1, 50);
    }

    @Test
    public void listAuditLogs_shouldRejectInvalidPaginationOrEnumParams() throws Exception {
        performList(Role.ADMIN, get("/api/admin/audits").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        performList(Role.ADMIN, get("/api/admin/audits").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        performList(Role.ADMIN, get("/api/admin/audits").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        performList(Role.ADMIN, get("/api/admin/audits").param("operation", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(adminAuditService);
    }

    @Test
    public void listAuditLogs_shouldRejectRegularUser() throws Exception {
        performList(Role.USER, get("/api/admin/audits"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminAuditService);
    }

    private ResultActions performList(Role role, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        String accessToken = jwtTokenService.createToken(123L, role, JwtTokenType.ACCESS);
        return mockMvc.perform(requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
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
