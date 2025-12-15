package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.model.Email;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender javaMailSender;

    @Async
    @Override
    public void send(Email email) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent(), true);

            javaMailSender.send(message);

            logger.info("E-mail enviado com sucesso para {}", email.getTo());
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail para {}: {}", email.getTo(), e.getMessage(), e);
        }
    }
}
