package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "signup_verifications")
public class SignupVerification {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(nullable = false)
    private Integer resendCount = 0;

    @Column(nullable = false)
    private LocalDateTime lastSentAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public void prepareNewCode(String name, String encodedPassword, String codeHash, LocalDateTime now, LocalDateTime expiresAt) {
        this.name = name;
        this.password = encodedPassword;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attemptCount = 0;
        this.resendCount = 0;
        this.lastSentAt = now;
        this.updatedAt = now;
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

}