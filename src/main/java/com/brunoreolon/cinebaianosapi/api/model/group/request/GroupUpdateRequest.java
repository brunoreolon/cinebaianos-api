package com.brunoreolon.cinebaianosapi.api.model.group.request;

import com.brunoreolon.cinebaianosapi.domain.model.GroupVisibility;
import com.brunoreolon.cinebaianosapi.domain.model.JoinPolicy;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class GroupUpdateRequest {

    @NotBlank
    private String name;

    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[A-Za-z0-9_]{4,6}$")
    private String tag;

    @NotBlank
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*$")
    private String slug;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GroupVisibility visibility;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JoinPolicy joinPolicy;

    @NotNull
    private boolean onlyAdminAddMovie;

    @NotNull
    private boolean allowGlobalVotes;

    @PositiveOrZero
    private int voteChangeDeadlineDays;

    @PositiveOrZero
    private int movieNewDays;

    @PositiveOrZero
    private int inviteMaxUses;

}