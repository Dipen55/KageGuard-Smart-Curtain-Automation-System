package com.kageguard.backend.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kageguard.alerts.email.enabled", havingValue = "true")
public class EmailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final String to;
    private final String from;

    public EmailNotifier(JavaMailSender mailSender,
                         @Value("${kageguard.alerts.email.to:}") String to,
                         @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.to = to;
        this.from = from;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public void send(String subject, String message) {
        if (to.isBlank() || from.isBlank()) {
            throw new IllegalStateException("Email address settings are missing in secrets.properties");
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
}