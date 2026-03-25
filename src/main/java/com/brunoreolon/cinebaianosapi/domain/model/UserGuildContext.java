package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_guild_contexts")
public class UserGuildContext {

    @EmbeddedId
    @Valid
    private UserGuildContextId contextId;

    @NotNull
    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userContext;

    @NotNull
    @MapsId("guildId")
    @ManyToOne
    @JoinColumn(name = "guild_id", nullable = false)
    private DiscordGuild guild;

    @ManyToOne
    @JoinColumn(name = "active_group_id")
    private Group group;

}