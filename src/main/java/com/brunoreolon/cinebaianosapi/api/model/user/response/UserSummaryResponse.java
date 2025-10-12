package com.brunoreolon.cinebaianosapi.api.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSummaryResponse {

    private String discordId;
    private String name;

}
