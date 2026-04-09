package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole;
import com.brunoreolon.cinebaianosapi.domain.event.SignupVerificationCodeRequestedEvent;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.UserAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.model.SignupVerification;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.SignupVerificationRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignupService {

    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_RESENDS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int CODE_EXPIRATION_MINUTES = 10;

    private final UserRepository userRepository;
    private final SignupVerificationRepository signupVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.context.ApplicationEventPublisher publisher;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void start(String name, String email, String rawPassword) {
        String normalizedName = normalizeName(name);
        String normalizedEmail = normalizeEmail(email);

        validateAlreadyRegistered(normalizedEmail);

        String verificationCode = generateNumericCode(CODE_LENGTH);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String codeHash = passwordEncoder.encode(verificationCode);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(CODE_EXPIRATION_MINUTES);

        SignupVerification verification = signupVerificationRepository.findById(normalizedEmail)
                .orElse(SignupVerification.builder().email(normalizedEmail).build());

        if (verification.getLastSentAt() != null && !verification.isExpired(now)) {
            long secondsSinceLastSend = java.time.Duration.between(verification.getLastSentAt(), now).getSeconds();
            if (secondsSinceLastSend < RESEND_COOLDOWN_SECONDS) {
                throw new BusinessException(
                        "signup.resend.cooldown.title",
                        "signup.resend.cooldown.message",
                        new Object[]{RESEND_COOLDOWN_SECONDS - secondsSinceLastSend},
                        HttpStatus.TOO_MANY_REQUESTS,
                        ApiErrorCode.SIGNUP_RESEND_COOLDOWN.asMap()
                );
            }
        }

        verification.prepareNewCode(normalizedName, encodedPassword, codeHash, now, expiresAt);
        signupVerificationRepository.save(verification);

        publisher.publishEvent(new SignupVerificationCodeRequestedEvent(
                normalizedEmail,
                normalizedName,
                verificationCode
        ));
    }

    @Transactional
    public void resend(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return;
        }

        SignupVerification verification = signupVerificationRepository.findById(normalizedEmail).orElse(null);
        if (verification == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (verification.isExpired(now)) {
            signupVerificationRepository.delete(verification);
            return;
        }

        long secondsSinceLastSend = java.time.Duration.between(verification.getLastSentAt(), now).getSeconds();
        if (secondsSinceLastSend < RESEND_COOLDOWN_SECONDS) {
            throw new BusinessException(
                    "signup.resend.cooldown.title",
                    "signup.resend.cooldown.message",
                    new Object[]{RESEND_COOLDOWN_SECONDS - secondsSinceLastSend},
                    HttpStatus.TOO_MANY_REQUESTS,
                    ApiErrorCode.SIGNUP_RESEND_COOLDOWN.asMap()
            );
        }

        if (verification.getResendCount() != null && verification.getResendCount() >= MAX_RESENDS) {
            throw new BusinessException(
                    "signup.resend.limit.title",
                    "signup.resend.limit.message",
                    new Object[]{MAX_RESENDS},
                    HttpStatus.TOO_MANY_REQUESTS,
                    ApiErrorCode.SIGNUP_RESEND_LIMIT.asMap()
            );
        }

        String verificationCode = generateNumericCode(CODE_LENGTH);
        verification.setCodeHash(passwordEncoder.encode(verificationCode));
        verification.setExpiresAt(now.plusMinutes(CODE_EXPIRATION_MINUTES));
        verification.setLastSentAt(now);
        verification.setUpdatedAt(now);
        verification.setAttemptCount(0);
        verification.setResendCount((verification.getResendCount() == null ? 0 : verification.getResendCount()) + 1);

        signupVerificationRepository.save(verification);

        publisher.publishEvent(new SignupVerificationCodeRequestedEvent(
                verification.getEmail(),
                verification.getName(),
                verificationCode
        ));
    }

    @Transactional
    public void verify(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = normalizeCode(code);

        SignupVerification verification = signupVerificationRepository.findById(normalizedEmail)
                .orElseThrow(this::invalidCodeException);

        LocalDateTime now = LocalDateTime.now();

        if (verification.isExpired(now)) {
            signupVerificationRepository.delete(verification);
            throw expiredCodeException();
        }

        if (verification.getAttemptCount() != null && verification.getAttemptCount() >= MAX_ATTEMPTS) {
            throw attemptsExceededException();
        }

        if (!passwordEncoder.matches(normalizedCode, verification.getCodeHash())) {
            int attempts = (verification.getAttemptCount() == null ? 0 : verification.getAttemptCount()) + 1;
            verification.setAttemptCount(attempts);
            verification.setUpdatedAt(now);
            signupVerificationRepository.save(verification);

            if (attempts >= MAX_ATTEMPTS) {
                throw attemptsExceededException();
            }

            throw invalidCodeException();
        }

        validateAlreadyRegistered(normalizedEmail);

        User user = User.builder()
                .name(verification.getName())
                .email(verification.getEmail())
                .password(verification.getPassword())
                .roles(Set.of(UserRole.USER))
                .active(true)
                .isBot(false)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyRegisteredException("user.email.registered.message", new Object[]{normalizedEmail});
        }

        signupVerificationRepository.delete(verification);
    }

    private void validateAlreadyRegistered(String normalizedEmail) {
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyRegisteredException("user.email.registered.message", new Object[]{normalizedEmail});
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim();
    }

    private String generateNumericCode(int length) {
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int value = secureRandom.nextInt((max - min) + 1) + min;
        return String.valueOf(value);
    }

    private BusinessException invalidCodeException() {
        return new BusinessException(
                "signup.code.invalid.title",
                "signup.code.invalid.message",
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.SIGNUP_CODE_INVALID.asMap()
        );
    }

    private BusinessException expiredCodeException() {
        return new BusinessException(
                "signup.code.expired.title",
                "signup.code.expired.message",
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.SIGNUP_CODE_EXPIRED.asMap()
        );
    }

    private BusinessException attemptsExceededException() {
        return new BusinessException(
                "signup.code.attempts.exceeded.title",
                "signup.code.attempts.exceeded.message",
                new Object[]{MAX_ATTEMPTS},
                HttpStatus.TOO_MANY_REQUESTS,
                ApiErrorCode.SIGNUP_ATTEMPTS_EXCEEDED.asMap()
        );
    }

}