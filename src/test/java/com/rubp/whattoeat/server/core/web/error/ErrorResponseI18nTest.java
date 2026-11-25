package com.rubp.whattoeat.server.core.web.error;

import com.rubp.whattoeat.server.feature.auth.config.SecurityConfig;
import com.rubp.whattoeat.server.testsupport.JwtTestKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErrorResponseI18nTest.ProbeController.class, properties = "spring.profiles.active=test")
@Import({SecurityConfig.class, ErrorResponseI18nTest.ProbeController.class})
public class ErrorResponseI18nTest {

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) throws Exception {
        JwtTestKeys keys = JwtTestKeys.generatePemKeyPair();
        registry.add("jwt.private-key-location", keys::privateKey);
        registry.add("jwt.public-key-location", keys::publicKey);
    }

    @Test
    public void mvcException_shouldDefaultToChineseWhenHeaderAbsent() throws Exception {
        mockMvc.perform(get("/api/test/probe").param("id", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("请求参数无效"));
    }

    @Test
    public void mvcException_shouldReturnChineseForZhHeader() throws Exception {
        mockMvc.perform(get("/api/test/probe")
                        .param("id", "invalid")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("请求参数无效"));
    }

    @Test
    public void mvcException_shouldReturnEnglishForEnHeader() throws Exception {
        mockMvc.perform(get("/api/test/probe")
                        .param("id", "invalid")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    public void mvcException_shouldNegotiatePriorityAndFallbackToChineseForUnsupportedLanguage() throws Exception {
        mockMvc.perform(get("/api/test/probe")
                        .param("id", "invalid")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "fr-FR, en;q=0.8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));

        mockMvc.perform(get("/api/test/probe")
                        .param("id", "invalid")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ja-JP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("请求参数无效"));
    }

    @Test
    public void securityUnauthorized_shouldReturnLocalizedMessage() throws Exception {
        mockMvc.perform(post("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"))
                .andExpect(jsonPath("$.message").value("access token 无效或已过期"));

        mockMvc.perform(post("/api/test/protected")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"))
                .andExpect(jsonPath("$.message").value("Access token is invalid or expired"));
    }

    @Test
    public void messageBundles_shouldHaveMatchingNonBlankEntries() throws IOException {
        Properties zhMessages = loadProperties("i18n/messages.properties");
        Properties enMessages = loadProperties("i18n/messages_en.properties");

        assertFalse(zhMessages.isEmpty());
        assertEquals(zhMessages.stringPropertyNames(), enMessages.stringPropertyNames());

        for (String key : zhMessages.stringPropertyNames()) {
            assertFalse(zhMessages.getProperty(key).isBlank(), "Blank zh message for key: " + key);
            assertFalse(enMessages.getProperty(key).isBlank(), "Blank en message for key: " + key);
        }
    }

    private Properties loadProperties(String path) throws IOException {
        Properties properties = new Properties();
        try (var reader = new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/test/probe")
        public String probe(@RequestParam("id") int id) {
            return Integer.toString(id);
        }

        @PostMapping("/api/test/protected")
        public void protectedEndpoint() {
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestConfig {
    }
}
