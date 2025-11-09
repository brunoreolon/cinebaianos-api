package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.CheckSecurity;
import com.brunoreolon.cinebaianosapi.domain.model.ResourceId;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.api.model.ValidationGroups.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRegistrationController {

    private final UserRegistratioService userRegistratioService;
    private final UserConverter userConverter;

    @PostMapping
    @CheckSecurity.IsAdminOrBot
    public ResponseEntity<UserDetailResponse> create(@Validated(UserCreateGroup.class) @RequestBody UserRequest userRequestuser) {
        User newUser = userRegistratioService.create(userConverter.toEntityFromCreate(userRequestuser));
        return ResponseEntity.status(HttpStatus.CREATED).body(userConverter.toDetailResponse(newUser));
    }

    @GetMapping
    @CheckSecurity.CanAccess
    public ResponseEntity<List<UserDetailResponse>> getAll(
            @RequestParam(value = "includeBot", required = false, defaultValue = "false") boolean includeBot
    ) {
        List<User> users = userRegistratioService.getAll();

        if (!includeBot) {
            users = users.stream()
                    .filter(user -> !user.isBot())
                    .toList();
        }

        return ResponseEntity.ok().body(userConverter.toDetailResponseList(users));
    }

    @GetMapping("/{discordId}")
    @CheckSecurity.CanAccess
    public ResponseEntity<UserDetailResponse> get(@PathVariable String discordId) {
        User user = userRegistratioService.get(discordId);
        return ResponseEntity.ok().body(userConverter.toDetailResponse(user));
    }

    @DeleteMapping("/{discordId}")
    @CheckSecurity.IsAdmin
    public ResponseEntity<Void> delete(@PathVariable @ResourceId String discordId) {
        userRegistratioService.delete(discordId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{discordId}")
    @CheckSecurity.IsOwnerOrAdmin(service = "userRegistratioService")
    public ResponseEntity<UserDetailResponse> update(@PathVariable @ResourceId String discordId,
                                                     @Valid @RequestBody UserUpdateRequest userRequest) {
        User userUpdate = userConverter.toEntityFromUpdate(userRequest);
        userUpdate.setDiscordId(discordId);

        User existingUser = userRegistratioService.get(discordId);

        existingUser = userConverter.merge(userUpdate, existingUser);

        User updated = userRegistratioService.update(existingUser);

        return ResponseEntity.ok().body(userConverter.toDetailResponse(updated));
    }

}