package com.brunoreolon.cinebaianosapi.api.model.movie.request;

import com.brunoreolon.cinebaianosapi.api.model.user.id.UserId;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class MovieSearchRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String year;

    @Valid
    @NotNull
    private UserId chooser;

    private Optional<VoteTypeId> vote = Optional.empty();

}
