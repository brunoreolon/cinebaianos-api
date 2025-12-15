package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.resend.ResendProperties;
import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.resend.Resend;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class ResendEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;
    private final ResendProperties resendProperties;

    @Async
    @Override
    public void send(Email email) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(resendProperties.getFrom())
                    .to(email.getTo())
                    .subject(email.getSubject())
                    .html(email.getContent())
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            logger.info("E-mail enviado via Resend para {} | id={}", email.getTo(), data.getId());

        } catch (ResendException e) {
            logger.error("Erro Resend ao enviar e-mail", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao enviar e-mail", e);
        }
    }
}

