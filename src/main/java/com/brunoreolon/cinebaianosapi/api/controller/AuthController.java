package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.auth.request.LoginRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.response.RefreshRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.response.TokenResponse;
import com.brunoreolon.cinebaianosapi.core.security.JwtService;
import com.brunoreolon.cinebaianosapi.domain.exception.ExpiredRefreshTokenException;
import com.brunoreolon.cinebaianosapi.domain.exception.InvalidRefreshTokenException;
import com.brunoreolon.cinebaianosapi.domain.model.RefreshToken;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.service.JwtBlacklistService;
import com.brunoreolon.cinebaianosapi.domain.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        String username = auth.getName();
        User user = userRepository.findByEmail(username).orElseThrow();
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();

        refreshTokenService.deleteByUser(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        String token = jwtService.generateToken(username, roles);

        return ResponseEntity.ok(new TokenResponse(token, refreshToken.getToken(), jwtService.getExpirationInSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        RefreshToken oldToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("The refresh token is invalid"));

        if (!refreshTokenService.isValid(oldToken)) {
            throw new ExpiredRefreshTokenException("The refresh token is expired or inactive");
        }

        User user = oldToken.getUser();
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();

        String newAccessToken = jwtService.generateToken(user.getEmail(), roles);
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);

//        refreshTokenService.deactivateToken(oldToken);

        return ResponseEntity.ok(new TokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtService.getExpirationInSeconds())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequest request,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        refreshTokenService.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenService::deactivateToken);

        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            String accessToken = authorization.substring(7).trim();
            jwtBlacklistService.blacklist(accessToken);
        }

        return ResponseEntity.noContent().build();
    }

}
