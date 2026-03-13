package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class UserGuildContextId {

    @NotNull
    @EqualsAndHashCode.Include
    private Long userId;

    @NotNull
    @EqualsAndHashCode.Include
    private Long guildId;

}
