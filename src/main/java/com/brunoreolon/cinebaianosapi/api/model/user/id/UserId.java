package com.brunoreolon.cinebaianosapi.api.model.user.id;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserId {

    @NotNull
    private String discordId;

}
