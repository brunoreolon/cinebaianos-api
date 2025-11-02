package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.JwtProperties;
import com.brunoreolon.cinebaianosapi.domain.model.RefreshToken;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepo;

    // Cria e salva um novo refresh token
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMinutes() * 60));
        token.setActive(true);
        return refreshTokenRepo.save(token);
    }

    // Rotaciona o token: desativa o antigo e cria um novo
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        if (oldToken != null && oldToken.isActive()) {
            oldToken.setActive(false);
            refreshTokenRepo.save(oldToken); // invalida o token usado
        }
        return createRefreshToken(oldToken.getUser());
    }

    public boolean isValid(RefreshToken token) {
        return token != null && token.isActive() && token.getExpiryDate().isAfter(Instant.now());
    }

    @Transactional
    public void deactivateToken(RefreshToken token) {
        token.setActive(false);
        refreshTokenRepo.save(token);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepo.deleteByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByTokenAndActiveTrue(token);
    }
}