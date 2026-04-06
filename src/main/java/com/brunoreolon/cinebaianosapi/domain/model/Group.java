package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
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
import java.util.HashSet;
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

    private boolean onlyAdminAddMovie = false;
    private boolean allowGlobalVotes = true;
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

//    @OneToMany(mappedBy = "group")
//    private List<VoteType> voteTypes = new ArrayList<>();

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
            throw new RuntimeException("Grupo já é ativo");

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new RuntimeException("Grupo já desativado");

        this.active = false;
    }

    @Override
    public Long getOwnerId() {
        return getOwner().getId();
    }

}