package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
public class Group {

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

    @ManyToOne
    @JoinColumn(name = "banned_by")
    private User bannedBy;

    private LocalDateTime bannedAt;

    private String bannedReason;

    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "group")
    private Set<GroupMovie> movies = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private List<VoteType> voteTypes = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupMemberBan> groupMemberBans = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupInvite> groupInvites = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private List<GroupJoinRequest> groupJoinRequests = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupGuildLinkRequest> groupGuildLinkRequests = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupGuild> guilds = new HashSet<>();

}