package com.brunoreolon.cinebaianosapi.api.model.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptGroupInviteRequest {

    @NotBlank
    private String token;

}