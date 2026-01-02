package com.brunoreolon.cinebaianosapi.api.model.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordRecoveryRequest {

    @NotBlank
    @Email
    private String email;

}
