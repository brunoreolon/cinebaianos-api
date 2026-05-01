package com.brunoreolon.cinebaianosapi.api.model.user.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserBanRequest {

    @NotBlank
    private String reason;

    @Future
    private LocalDateTime expiresAt;

}