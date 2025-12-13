package com.brunoreolon.cinebaianosapi.api.model.vote.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteTypeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex code, e.g. #00FF00")
    private String color;

    private String emoji;

}
