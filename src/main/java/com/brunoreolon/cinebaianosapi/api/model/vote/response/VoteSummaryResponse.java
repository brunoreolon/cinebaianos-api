package com.brunoreolon.cinebaianosapi.api.model.vote.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VoteSummaryResponse {

    private Long id;
    private String description;
    private String color;
    private String emoji;
    private LocalDateTime votedAt;

}
