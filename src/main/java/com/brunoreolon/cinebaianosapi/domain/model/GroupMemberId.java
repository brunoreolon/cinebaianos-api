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
public class GroupMemberId {

    @NotNull
    @EqualsAndHashCode.Include
    private Long memberId;

    @NotNull
    @EqualsAndHashCode.Include
    private Long groupId;

}