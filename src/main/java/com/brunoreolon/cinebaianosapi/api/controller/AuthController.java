package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.auth.request.LoginRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.request.PasswordRecoveryRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.request.ResetPasswordRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.response.RefreshRequest;
import com.brunoreolon.cinebaianosapi.api.model.auth.response.TokenResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.service.JwtService;
import com.brunoreolon.cinebaianosapi.domain.exception.ExpiredRefreshTokenException;
import com.brunoreolon.cinebaianosapi.domain.exception.InvalidRefreshTokenException;
import com.brunoreolon.cinebaianosapi.domain.model.PasswordResetToken;
import com.brunoreolon.cinebaianosapi.domain.model.RefreshToken;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.service.JwtBlacklistService;
import com.brunoreolon.cinebaianosapi.domain.service.PasswordRecoveryService;
import com.brunoreolon.cinebaianosapi.domain.service.RefreshTokenService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação", description = "Operações relacionadas à autenticação e recuperação de senha.")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;
    private final PasswordRecoveryService recoveryService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Login do usuário", description = "Autentica um usuário e retorna tokens de acesso e refresh.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Dados de login do usuário")
            @RequestBody @Valid LoginRequest req) {
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
    @Operation(summary = "Atualizar token de acesso", description = "Gera um novo token de acesso e refresh usando o refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TokenResponse> refresh(
            @Parameter(description = "Refresh token do usuário")
            @RequestBody @Valid RefreshRequest request) {
        RefreshToken oldToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("invalid.refresh.token.message"));

        if (!refreshTokenService.isValid(oldToken)) {
            throw new ExpiredRefreshTokenException("expired.refresh.token.message");
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
    @Operation(summary = "Logout do usuário", description = "Desativa o refresh token e coloca o access token na blacklist.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token do usuário")
            @RequestBody @Valid RefreshRequest request,

            @Parameter(description = "Access token do usuário (Authorization header)")
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        refreshTokenService.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenService::deactivateToken);

        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            String accessToken = authorization.substring(7).trim();
            jwtBlacklistService.blacklist(accessToken);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recover")
    @Operation(summary = "Recuperação de senha", description = "Envia email para o usuário com token para redefinir senha.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email de recuperação enviado"),
            @ApiResponse(responseCode = "404", description = "Email não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> recoverPassword(
            @Parameter(description = "Email do usuário para recuperação de senha")
            @RequestBody @Valid PasswordRecoveryRequest recoveryRequest) {
        recoveryService.recover(recoveryRequest.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário utilizando o token recebido no email de recuperação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Token de recuperação e nova senha")
            @RequestBody @Valid ResetPasswordRequest request) {
        PasswordResetToken resetToken =
                recoveryService.validate(request.getToken());

        userService.resetPasswordByRecover(
                resetToken.getUser().getDiscordId(),
                request.getNewPassword()
        );

        recoveryService.markAsUsed(resetToken);

        return ResponseEntity.noContent().build();
    }

}
