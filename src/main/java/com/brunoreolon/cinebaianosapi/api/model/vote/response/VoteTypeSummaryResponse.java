package com.brunoreolon.cinebaianosapi.api.model.vote.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VoteTypeSummaryResponse {

    private Long id;
    private String description;
    private String color;
    private String emoji;

}
