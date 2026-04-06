package com.brunoreolon.cinebaianosapi.api.model.group.request;

import com.brunoreolon.cinebaianosapi.domain.model.GroupVisibility;
import com.brunoreolon.cinebaianosapi.domain.model.JoinPolicy;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 4, max = 6)
    private String tag;

    @NotBlank
    @Size(min = 4, max = 30)
    private String slug;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GroupVisibility visibility;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JoinPolicy joinPolicy;

    private boolean onlyAdminAddMovie;
    private boolean allowGlobalVotes;

    @PositiveOrZero
    private int voteChangeDeadlineDays;

    @PositiveOrZero
    private int movieNewDays;

    @PositiveOrZero
    private int inviteMaxUses;

}