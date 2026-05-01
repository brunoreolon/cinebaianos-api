package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.Ownable;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "groups")
public class Group implements Ownable<Long> {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 4, max = 6)
    @Column(unique = true)
    private String tag;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    private GroupVisibility visibility;

    @Enumerated(EnumType.STRING)
    private JoinPolicy joinPolicy;

    private Boolean onlyAdminAddMovie = false;
    private Boolean allowGlobalVotes = true;
    private int voteChangeDeadlineDays;
    private int movieNewDays;
    private int inviteMaxUses;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "banned_by")
    private User bannedBy;

    private LocalDateTime bannedAt;

    private String banReason;

    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "group")
    private Set<GroupMovie> movies = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private List<VoteType> voteTypes = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("member.id ASC")
    private Set<GroupMember> members = new HashSet<>();

    public Boolean canActivate() {
        return !this.getActive();
    }

    public Boolean canDisable() {
        return this.getActive();
    }

    public void activate() {
        if (!this.canActivate())
            throw new BusinessException(
                    "group.cannot.activate.title",
                    "group.cannot.activate.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "group.cannot.disable.title",
                    "group.cannot.disable.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());

        this.active = false;
    }

    @Override
    public Long getOwnerId() {
        return getOwner().getId();
    }

    public boolean isBanned() {
        return bannedAt != null && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    public void ban(User bannedBy, String reason, LocalDateTime expiresAt) {
        this.bannedBy = bannedBy;
        this.banReason = reason;
        this.bannedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;

        if (expiresAt == null) {
            this.active = false;
        }
    }

    public void unban() {
        this.bannedBy = null;
        this.banReason = null;
        this.bannedAt = null;
        this.expiresAt = null;
        this.active = true;
    }

}