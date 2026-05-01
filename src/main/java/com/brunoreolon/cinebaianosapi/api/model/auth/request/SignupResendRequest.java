package com.brunoreolon.cinebaianosapi.api.model.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupResendRequest {

    @NotBlank
    @Email
    private String email;

}