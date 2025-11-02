package com.brunoreolon.cinebaianosapi.api.model.vote.id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VoteKey {

    private String discordId;
    private Long movieId;

}
