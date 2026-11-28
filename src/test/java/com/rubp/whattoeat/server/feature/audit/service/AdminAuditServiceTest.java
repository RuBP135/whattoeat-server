package com.rubp.whattoeat.server.feature.audit.service;

import com.rubp.whattoeat.server.core.config.I18nConfig;
import com.rubp.whattoeat.server.core.model.PageResponse;
import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.domain.AccountStatus;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogEntry;
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogRepository;
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation;
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType;
import com.rubp.whattoeat.server.feature.audit.model.AdminAuditLogResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminAuditServiceTest {

    private final AdminAuditLogRepository adminAuditLogRepository = mock(AdminAuditLogRepository.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-26T12:00:00Z"), ZoneOffset.UTC);
    private final MessageSource messageSource = new I18nConfig().messageSource();
    private final AdminAuditService adminAuditService = new AdminAuditService(
            adminAuditLogRepository,
            accountRepository,
            clock,
            messageSource
    );

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void listAuditLogs_shouldRenderLocalizedSummaryAndMapPageResponse() {
        Instant createdAt = Instant.parse("2026-11-26T12:00:00Z");
        AccountEntry admin = new AccountEntry("admin", "hash", Role.ADMIN, AccountStatus.ACTIVE, createdAt);
        ReflectionTestUtils.setField(admin, "id", 7L);

        AdminAuditLogEntry entry = new AdminAuditLogEntry(
                99L,
                admin,
                AuditResourceType.STALL,
                12L,
                AuditOperation.CREATE,
                "audit.stall.create",
                List.of("川味面馆", "一食堂", "3"),
                createdAt
        );

        LocaleContextHolder.setLocale(Locale.CHINESE);
        when(adminAuditLogRepository.countAll(AuditOperation.CREATE, AuditResourceType.STALL)).thenReturn(1L);
        when(adminAuditLogRepository.findPage(AuditResourceType.STALL, AuditOperation.CREATE, 20, 0))
                .thenReturn(List.of(entry));

        PageResponse<AdminAuditLogResponse> zhResult = adminAuditService.listAuditLogs(
                AuditOperation.CREATE,
                AuditResourceType.STALL,
                0,
                20
        );

        verify(adminAuditLogRepository).countAll(AuditOperation.CREATE, AuditResourceType.STALL);
        verify(adminAuditLogRepository).findPage(AuditResourceType.STALL, AuditOperation.CREATE, 20, 0);
        assertEquals(0, zhResult.getPage());
        assertEquals(20, zhResult.getSize());
        assertEquals(1L, zhResult.getTotal());
        assertEquals(1, zhResult.getTotalPages());
        assertEquals(1, zhResult.getItems().size());

        AdminAuditLogResponse zhItem = zhResult.getItems().getFirst();
        assertEquals(99L, zhItem.getId());
        assertEquals(7L, zhItem.getAdminId());
        assertEquals("admin", zhItem.getAdminName());
        assertEquals(AuditOperation.CREATE, zhItem.getOperation());
        assertEquals(AuditResourceType.STALL, zhItem.getResourceType());
        assertEquals(12L, zhItem.getResourceId());
        assertEquals("新建档口「川味面馆」（地点：一食堂，铺位：3）", zhItem.getSummary());
        assertEquals(createdAt, zhItem.getCreatedAt());

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        when(adminAuditLogRepository.countAll(null, null)).thenReturn(11L);
        when(adminAuditLogRepository.findPage(null, null, 10, 10)).thenReturn(List.of(entry));

        PageResponse<AdminAuditLogResponse> enResult = adminAuditService.listAuditLogs(null, null, 1, 10);

        verify(adminAuditLogRepository).countAll(null, null);
        verify(adminAuditLogRepository).findPage(null, null, 10, 10);
        assertEquals(1, enResult.getPage());
        assertEquals(10, enResult.getSize());
        assertEquals(11L, enResult.getTotal());
        assertEquals(2, enResult.getTotalPages());
        assertEquals("Created stall \"川味面馆\" (location: 一食堂, slot: 3)", enResult.getItems().getFirst().getSummary());
    }

    @Test
    public void listAuditLogs_shouldFallbackToSummaryKeyWhenMessageMissing() {
        Instant createdAt = Instant.parse("2026-11-26T12:00:00Z");
        AccountEntry admin = new AccountEntry(null, "hash", Role.ADMIN, AccountStatus.ACTIVE, createdAt);
        ReflectionTestUtils.setField(admin, "id", 7L);

        AdminAuditLogEntry entry = new AdminAuditLogEntry(
                100L,
                admin,
                AuditResourceType.ACCOUNT,
                null,
                AuditOperation.BAN,
                "audit.legacy.unknown",
                List.of("ignored"),
                createdAt
        );

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        when(adminAuditLogRepository.countAll(null, null)).thenReturn(1L);
        when(adminAuditLogRepository.findPage(null, null, 20, 0)).thenReturn(List.of(entry));

        PageResponse<AdminAuditLogResponse> result = adminAuditService.listAuditLogs(null, null, 0, 20);

        AdminAuditLogResponse item = result.getItems().getFirst();
        assertNull(item.getAdminName());
        assertNull(item.getResourceId());
        assertEquals(AuditOperation.BAN, item.getOperation());
        assertEquals(AuditResourceType.ACCOUNT, item.getResourceType());
        assertEquals("audit.legacy.unknown", item.getSummary());
    }

    @Test
    public void listAuditLogs_shouldSkipFindPageWhenTotalZeroOrOffsetExceedsTotal() {
        when(adminAuditLogRepository.countAll(null, null)).thenReturn(0L);
        PageResponse<AdminAuditLogResponse> emptyResult = adminAuditService.listAuditLogs(null, null, 0, 20);

        assertTrue(emptyResult.getItems().isEmpty());
        assertEquals(0, emptyResult.getPage());
        assertEquals(20, emptyResult.getSize());
        assertEquals(0L, emptyResult.getTotal());
        assertEquals(0, emptyResult.getTotalPages());

        when(adminAuditLogRepository.countAll(AuditOperation.DELETE, null)).thenReturn(15L);
        PageResponse<AdminAuditLogResponse> outOfRangeResult =
                adminAuditService.listAuditLogs(AuditOperation.DELETE, null, 2, 10);

        assertTrue(outOfRangeResult.getItems().isEmpty());
        assertEquals(2, outOfRangeResult.getPage());
        assertEquals(10, outOfRangeResult.getSize());
        assertEquals(15L, outOfRangeResult.getTotal());
        assertEquals(2, outOfRangeResult.getTotalPages());

        verify(adminAuditLogRepository, never()).findPage(any(), any(), anyInt(), anyInt());
    }
}
