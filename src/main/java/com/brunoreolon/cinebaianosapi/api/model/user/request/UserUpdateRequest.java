package com.brunoreolon.cinebaianosapi.api.model.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {

    private String name;

    @Email
    private String email;

    @Size(min = 6)
    private String password;

    private String avatar;

    private String biography;

}
