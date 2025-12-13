package com.brunoreolon.cinebaianosapi.api.model.user.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusActiveAccountUpdateRequest {

    @NotNull
    private Boolean active;

}
