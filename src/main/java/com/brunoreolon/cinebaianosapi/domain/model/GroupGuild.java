package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class GroupGuild {

    @EmbeddedId
    @Valid
    private GroupGuildId groupGuildId;

    @NotNull
    @MapsId("groupId")
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @NotNull
    @MapsId("guildId")
    @ManyToOne
    @JoinColumn(name = "guild_id")
    private DiscordGuild guild;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime linkedAt;

}