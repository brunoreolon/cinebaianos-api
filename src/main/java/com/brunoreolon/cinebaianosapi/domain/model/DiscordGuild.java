package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "discord_guilds")
public class DiscordGuild {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "guild_id", unique = true)
    private Long guild;

    @NotBlank
    private String name;

    private Boolean active = true;

    @CreationTimestamp()
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "guild")
    private List<GroupGuild> groupsGuilds = new ArrayList<>();

    @OneToMany(mappedBy = "discordGuild")
    private List<GroupGuildLinkRequest> groupGuildLinkRequests = new ArrayList<>();

}