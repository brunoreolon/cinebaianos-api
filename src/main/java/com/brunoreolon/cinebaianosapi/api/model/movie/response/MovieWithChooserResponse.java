package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
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
public class MovieWithChooserResponse {

    private Long id;
    private String title;
    private String year;
    private String tmdbId;
    private LocalDateTime dateAdded;
    private String posterPath;
    private String synopsis;
    private String director;
    private List<GenreResponse> genres;
    private UserDetailResponse chooser;
    private List<UsersVotesSummaryResponse> votes;

}
