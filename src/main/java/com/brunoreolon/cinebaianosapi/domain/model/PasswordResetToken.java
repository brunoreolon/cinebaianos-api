package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @Column(length = 36)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discord_id")
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    public PasswordResetToken(User user, Instant expiresAt) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markAsUsed() {
        this.used = true;
    }

}