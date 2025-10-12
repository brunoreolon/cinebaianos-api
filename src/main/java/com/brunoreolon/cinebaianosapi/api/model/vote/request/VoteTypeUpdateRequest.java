package com.brunoreolon.cinebaianosapi.api.model.vote.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteTypeUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

}
