package com.brunoreolon.cinebaianosapi.api.model.genre.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenreStatsResponse {

    private String name;
    private Integer total;

}
