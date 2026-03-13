package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class UserGuildContext {

    @EmbeddedId
    @Valid
    private UserGuildContextId contextId;

    @NotNull
    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @MapsId("guildId")
    @ManyToOne
    @JoinColumn(name = "guild_id")
    private DiscordGuild guild;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "active_group_id")
    private Group group;

}