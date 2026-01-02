package com.brunoreolon.cinebaianosapi.api.model.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResetPasswordRequest {

    @NotBlank
    private String newPassword;
}
