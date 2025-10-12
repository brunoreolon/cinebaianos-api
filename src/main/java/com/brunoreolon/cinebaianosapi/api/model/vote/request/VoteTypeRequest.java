package com.brunoreolon.cinebaianosapi.api.model.vote.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteTypeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean active;

}
