package com.brunoreolon.cinebaianosapi.api.model.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDetailResponse {

    private String discordId;
    private String name;
    private String email;
    private LocalDateTime Joined;

}
