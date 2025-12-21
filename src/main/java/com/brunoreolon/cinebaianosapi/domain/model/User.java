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

@Entity
@Table(name = "users")
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Ownable<String> {

    @Id
    @EqualsAndHashCode.Include
    private String discordId;

    @NotBlank
    private String name;

    @Column(unique = true)
    @NotBlank
    @Email
    private String email;

    @Size(min = 8)
    private String password;

    @CreationTimestamp()
    @Column(updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime updated;

    @OneToMany(mappedBy = "chooser")
    List<Movie> movies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="discord_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new LinkedHashSet<>();

    private Boolean isBot;
    private String avatar;
    private String biography;
    private Boolean active;

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
                    "You cannot activate a user that is already active.",
                    HttpStatus.BAD_REQUEST,
                    "User cannot be activated",
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "You cannot deactivate a user that is already deactivated.",
                    HttpStatus.BAD_REQUEST,
                    "User cannot be disabled",
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
