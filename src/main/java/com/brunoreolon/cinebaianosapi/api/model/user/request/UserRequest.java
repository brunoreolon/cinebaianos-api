package com.brunoreolon.cinebaianosapi.api.model.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.brunoreolon.cinebaianosapi.api.model.ValidationGroups.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRequest {

    @NotBlank(groups = {UserCreateGroup.class})
    private String discordId;

    @NotBlank(groups = {UserCreateGroup.class, UserUpdateGroup.class})
    private String name;

    @NotBlank(groups = {UserCreateGroup.class, UserUpdateGroup.class})
    @Email
    private String email;

    @NotBlank(groups = {UserUpdateGroup.class})
    @Size(min = 6)
    private String password;

}
