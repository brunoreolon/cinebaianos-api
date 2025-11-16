package com.brunoreolon.cinebaianosapi.api.model.auth.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshRequest {

    @NotBlank
    private String refreshToken;

}
