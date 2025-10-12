package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieSummaryResponse {

    private Long id;
    private String title;
    private String tmbdId;

}
