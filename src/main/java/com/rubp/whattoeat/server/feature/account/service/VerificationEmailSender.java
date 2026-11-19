package com.rubp.whattoeat.server.feature.account.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final long verificationTtlMinutes;

    public VerificationEmailSender(
            JavaMailSender mailSender,
            @Value("${account.email.from}") String from,
            @Value("${account.email.verification-ttl}") Duration verificationTtl
    ) {
        long minutes = verificationTtl.toMinutes();
        if (minutes <= 0 || !verificationTtl.equals(Duration.ofMinutes(minutes))) {
            throw new IllegalStateException("Account email verification TTL must be a positive whole number of minutes");
        }

        this.mailSender = mailSender;
        this.from = from;
        this.verificationTtlMinutes = minutes;
    }

    public void sendVerificationCode(String email, String code) {
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("对策选饭/WhatToEat验证码");
        message.setText("您的验证码是 " + code + "，\n"
                + "该验证码" + verificationTtlMinutes + "分钟内有效，请勿泄露给他人。");
        mailSender.send(message);
    }
}
