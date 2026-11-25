package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.core.config.I18nConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class VerificationEmailSenderTest {

    private static final String FROM = "noreply@example.com";
    private static final String TO = "user@example.com";
    private static final String CODE = "123456";

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final VerificationEmailSender sender = new VerificationEmailSender(
            mailSender,
            new I18nConfig().messageSource(),
            FROM,
            Duration.ofMinutes(5)
    );

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void sendVerificationCode_shouldFormatChineseEmail() {
        LocaleContextHolder.setLocale(Locale.CHINESE);

        sender.sendVerificationCode(TO, CODE);

        SimpleMailMessage message = captureSentMessage();
        assertEquals(FROM, message.getFrom());
        assertArrayEquals(new String[]{TO}, message.getTo());
        assertEquals("对策选饭验证码", message.getSubject());
        assertEquals("您的验证码是 123456，\n该验证码5分钟内有效，请勿泄露给他人。", message.getText());
    }

    @Test
    public void sendVerificationCode_shouldFormatEnglishEmail() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        sender.sendVerificationCode(TO, CODE);

        SimpleMailMessage message = captureSentMessage();
        assertEquals(FROM, message.getFrom());
        assertArrayEquals(new String[]{TO}, message.getTo());
        assertEquals("WhatToEat Verification Code", message.getSubject());
        assertEquals(
                "Your verification code is 123456.\nThis code is valid for 5 minutes. Please do not share it with anyone.",
                message.getText()
        );
    }

    private SimpleMailMessage captureSentMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
