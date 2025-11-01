package com.brunoreolon.cinebaianosapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class MovieVotes {

    private final Movie movie;
    private final List<Vote> votes;

}
