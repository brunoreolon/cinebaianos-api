package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@DynamicUpdate
@Entity
@Table(name = "users")
public class User implements Ownable<Long> {

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
    @Column(unique = true)
    private String email;

    @Size(min = 8)
    @NotBlank
    private String password;

    private String avatar;

    private String biography;

    private Boolean isBot = false;

    private Boolean active = true;

    @CreationTimestamp()
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime bannedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by")
    private User bannedBy;

    private String banReason;

    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "chooser")
    List<Movie> movies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner")
    private List<Group> ownedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "bannedBy")
    private List<Group> bannedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "chooser")
    private List<GroupMovie> moviess = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<GroupMember> members = new ArrayList<>();

    public Boolean hasRole(UserRole userRole) {
        return roles.contains(userRole);
    }

    public Boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
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
    public Long getOwnerId() {
        return getId();
    }

    public void AddAdmin() {
        if (!this.hasRole(UserRole.ADMIN)) {
            this.getRoles().add(UserRole.ADMIN);
        }
    }

    public void RemoveAdmin() {
        this.getRoles().remove(UserRole.ADMIN);
    }

}