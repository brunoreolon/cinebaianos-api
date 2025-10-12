package com.brunoreolon.cinebaianosapi.api.model.movie.request;

import com.brunoreolon.cinebaianosapi.api.model.movie.id.MovieId;
import com.brunoreolon.cinebaianosapi.api.model.user.id.UserId;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieIdRequest {

    @Valid
    @NotNull
    private MovieId movie;

    @Valid
    @NotNull
    private UserId chooser;

    private Optional<VoteTypeId> vote = Optional.empty();

}
