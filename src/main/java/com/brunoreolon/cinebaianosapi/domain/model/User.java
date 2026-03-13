package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@DynamicUpdate
@Entity
@Table(name = "users")
public class User implements Ownable<String> {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String discordId;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min = 16)
    private String password;

    @CreationTimestamp()
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated;

    @OneToMany(mappedBy = "chooser")
    private List<Movie> movies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new LinkedHashSet<>();

    private Boolean isBot = false;
    private String avatar;
    private String biography;
    private Boolean active = true;

    private LocalDateTime bannedAt;

    @ManyToOne
    private User bannedBy;

    private String banReason;
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "owner")
    private List<Group> ownedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<GroupInvite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "invitedUser")
    private List<GroupInvite> invitedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserGuildContext> userGuildContexts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<GroupJoinRequest> groupJoinRequests = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<GroupGuildLinkRequest> groupGuildLinkRequests = new ArrayList<>();

    public Boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public Boolean canActivate() {
        return !this.getActive();
    }

    public Boolean canDisable() {
        return this.getActive();
    }

    public void activate() {
        if (!this.canActivate())
            throw new BusinessException(
                    "user.cannot.activate.title",
                    "user.cannot.activate.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "user.cannot.disable.title",
                    "user.cannot.disable.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = false;
    }

    @Override
    public String getOwnerId() {
        return getDiscordId();
    }

    public void AddAdmin() {
        if (!this.hasRole(Role.ADMIN)) {
            this.getRoles().add(Role.ADMIN);
        }
    }

    public void RemoveAdmin() {
        this.getRoles().remove(Role.ADMIN);
    }
}