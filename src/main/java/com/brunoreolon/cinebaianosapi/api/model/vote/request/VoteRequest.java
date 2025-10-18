package com.brunoreolon.cinebaianosapi.api.model.vote.request;

import com.brunoreolon.cinebaianosapi.api.model.movie.id.MovieId;
import com.brunoreolon.cinebaianosapi.api.model.user.id.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {

    @Valid
    @NotNull
    private UserId voter;

    @Valid
    @NotNull
    private MovieId movie;

    @NotNull
    private Long vote;

}
