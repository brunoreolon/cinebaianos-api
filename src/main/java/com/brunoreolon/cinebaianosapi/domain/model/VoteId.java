package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VoteId {

    @NotNull
    @EqualsAndHashCode.Include
    private Long movieId;

    @NotNull
    @EqualsAndHashCode.Include
    private String voterId;

}
