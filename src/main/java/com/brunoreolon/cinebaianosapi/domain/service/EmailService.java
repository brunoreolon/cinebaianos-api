package com.brunoreolon.cinebaianosapi.domain.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Async
    public void send(String to, String newPassword) {
        try {
            String htmlContent = "<html><body>"
                    + "<p>Olá!</p>"
                    + "<p>Sua conta no <b>Cinebaianos</b> foi criada com sucesso.</p>"
                    + "<p>Sua senha temporária é: <b>" + newPassword + "</b></p>"
                    + "<p>Por favor, clique no link abaixo para fazer o primeiro login e alterar sua senha:</p>"
                    + "<p><a href='" + frontendUrl + "'>" + frontendUrl + "</a></p>"
                    + "<p>Atenciosamente,<br>Equipe Cinebaianos</p>"
                    + "</body></html>";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Sua senha temporária");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            logger.info("E-mail enviado com sucesso para {}", to);
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail para {}: {}", to, e.getMessage(), e);
        }
    }
}
