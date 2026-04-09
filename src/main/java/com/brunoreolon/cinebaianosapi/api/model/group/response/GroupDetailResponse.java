package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.GroupVisibility;
import com.brunoreolon.cinebaianosapi.domain.model.JoinPolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupDetailResponse {

    private Long id;
    private String name;
    private String tag;
    private String slug;
    private UserSummaryResponse owner;
    private boolean active;
    private boolean banned;
    private LocalDateTime bannedAt;
    private String banReason;
    private LocalDateTime expiresAt;
    private GroupVisibility visibility;
    private JoinPolicy joinPolicy;
    private boolean onlyAdminAddMovie;
    private boolean allowGlobalVotes;
    private int voteChangeDeadlineDays;
    private int movieNewDays;
    private int inviteMaxUses;
    private LocalDateTime createdAt;
    private List<MovieWithChooserResponse> movies;

}