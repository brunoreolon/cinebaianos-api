package com.brunoreolon.cinebaianosapi.api.model.user.stats;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSummaryResponse {

    private UserDetailResponse userDetailResponse;
    private UserStats userStats;

}