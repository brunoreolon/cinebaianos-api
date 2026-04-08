package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "group_invites")
public class GroupInvite {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_type", nullable = false, length = 30)
    private GroupInviteType inviteType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GroupInviteStatus status;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @Column(name = "uses_count", nullable = false)
    private Integer usesCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public boolean isPending() {
        return this.status == GroupInviteStatus.PENDING;
    }

    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean hasRemainingUses() {
        return usesCount < maxUses;
    }

    public boolean canBeUsedBy(Long userId) {
        if (invitedUser == null) {
            return true;
        }
        return invitedUser.getId().equals(userId);
    }

    public void consumeUse() {
        this.usesCount = this.usesCount + 1;
        if (!hasRemainingUses()) {
            this.status = GroupInviteStatus.EXPIRED;
        }
    }

    public void revoke() {
        this.status = GroupInviteStatus.REVOKED;
    }

    public void expire() {
        this.status = GroupInviteStatus.EXPIRED;
    }

}