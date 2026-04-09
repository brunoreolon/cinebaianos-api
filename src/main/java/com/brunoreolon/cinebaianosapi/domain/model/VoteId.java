package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
public class VoteId {

    @NotNull
    @EqualsAndHashCode.Include
    private Long movieId;

    @NotNull
    @EqualsAndHashCode.Include
    private Long voterId;

}