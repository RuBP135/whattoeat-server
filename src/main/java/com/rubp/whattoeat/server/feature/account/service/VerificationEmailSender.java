package com.rubp.whattoeat.server.feature.account.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final String from;
    private final long verificationTtlMinutes;

    public VerificationEmailSender(
            JavaMailSender mailSender,
            MessageSource messageSource,
            @Value("${account.email.from}") String from,
            @Value("${account.email.verification-ttl}") Duration verificationTtl
    ) {
        long minutes = verificationTtl.toMinutes();
        if (minutes <= 0 || !verificationTtl.equals(Duration.ofMinutes(minutes))) {
            throw new IllegalStateException("Account email verification TTL must be a positive whole number of minutes");
        }

        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.from = from;
        this.verificationTtlMinutes = minutes;
    }

    public void sendVerificationCode(String email, String code) {
        Locale locale = LocaleContextHolder.getLocale();
        String subject = messageSource.getMessage(
                "account.email.verification_subject",
                null,
                locale
        );
        String text = messageSource.getMessage(
                "account.email.verification_body",
                new Object[]{code, Long.toString(verificationTtlMinutes)},
                locale
        );

        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
