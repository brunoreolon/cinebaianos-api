package com.brunoreolon.cinebaianosapi.api.model.vote.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteTypeStatusUpdateRequest {

    @NotNull
    private Boolean active;

}
