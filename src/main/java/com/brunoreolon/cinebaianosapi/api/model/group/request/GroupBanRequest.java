package com.brunoreolon.cinebaianosapi.api.model.group.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupBanRequest {

    @NotBlank
    private String reason;

    @Future
    private LocalDateTime expiresAt;
}