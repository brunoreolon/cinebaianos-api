package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class DiscordGuild {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "guild_id", unique = true)
    private Long guild;

    @NotBlank
    private String name;

    private Boolean active = true;

    @OneToMany(mappedBy = "guild")
    private List<GroupGuild> groupsGuilds = new ArrayList<>();

    @OneToMany(mappedBy = "discordGuild")
    private List<GroupGuildLinkRequest> groupGuildLinkRequests = new ArrayList<>();

    @CreationTimestamp()
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime created;

}