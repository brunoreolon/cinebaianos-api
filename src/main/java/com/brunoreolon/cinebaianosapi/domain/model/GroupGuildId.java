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
public class GroupGuildId {

    @NotNull
    @EqualsAndHashCode.Include
    private Long groupId;

    @NotNull
    @EqualsAndHashCode.Include
    private Long guildId;

}