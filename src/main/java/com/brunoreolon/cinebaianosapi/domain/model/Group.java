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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "group")
    private List<Movie> movies = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<VoteType> voteTypes = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupInvite> groupInvites = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupGuild> guilds = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupBan> groupBans = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private List<GroupJoinRequest> groupJoinRequests = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<GroupGuildLinkRequest> groupGuildLinkRequests = new ArrayList<>();

    private Boolean active = true;
    private String visibility;

    @Enumerated(EnumType.STRING)
    private JoinPolicy joinPolicy;

    private Boolean onlyAdminAddMovie = false;
    private Boolean allowGlobalVotes = true;
    private Integer voteChangeDeadlineDays;
    private Integer movieNewDays;
    private Integer inviteMaxUses;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

}