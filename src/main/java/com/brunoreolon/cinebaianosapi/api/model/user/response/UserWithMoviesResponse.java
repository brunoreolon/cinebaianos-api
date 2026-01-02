package com.brunoreolon.cinebaianosapi.api.model.user.response;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserWithMoviesResponse {

    private UserDetailResponse user;
    private List<MovieDetailResponse> movies;

}
