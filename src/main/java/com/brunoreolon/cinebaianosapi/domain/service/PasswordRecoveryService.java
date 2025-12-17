package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.ResetTokenException;
import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.brunoreolon.cinebaianosapi.domain.model.PasswordResetToken;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.PasswordResetTokenRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.util.EmailUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private final EmailService emailService;
    private final EmailUtil emailUtil;
    private final PasswordResetTokenRepository repository;
    private final UserRepository userRepository;

    /**
     * Inicia o fluxo de recuperação de senha.
     * Sempre executa de forma segura (não revela se o email existe).
     */
    @Transactional
    public void recover(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            PasswordResetToken token = create(user);

            Email mail = emailUtil.recoverPassword(user, token.getToken());
            emailService.send(mail);
        });
    }

    /**
     * Cria um novo token de redefinição para o usuário
     */
    @Transactional
    public PasswordResetToken create(User user) {
        repository.deleteByUserDiscordId(user.getDiscordId());

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        PasswordResetToken token = new PasswordResetToken(user, expiresAt);

        return repository.save(token);
    }

    /**
     * Valida token (existência, expiração e uso)
     */
    public PasswordResetToken validate(String token) {
        PasswordResetToken resetToken = repository.findByToken(token)
                .orElseThrow(ResetTokenException::new);

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new ResetTokenException();
        }

        return resetToken;
    }

    /**
     * Marca token como usado após redefinição
     */
    @Transactional
    public void markAsUsed(PasswordResetToken token) {
        token.markAsUsed();
        repository.save(token);
    }

}
