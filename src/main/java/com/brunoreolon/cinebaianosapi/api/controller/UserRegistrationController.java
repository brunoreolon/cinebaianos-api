package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.api.model.ValidationGroups.*;
import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRegistrationController {

    private final UserRegistratioService userRegistratioService;
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @PostMapping
    @RequireRole(roles = {Role.ADMIN}, allowBot = true)
    public ResponseEntity<UserDetailResponse> create(@Validated(UserCreateGroup.class) @RequestBody UserRequest userRequestuser) {
        User newUser = userRegistratioService.create(userConverter.toEntityFromCreate(userRequestuser));
        return ResponseEntity.status(HttpStatus.CREATED).body(userConverter.toDetailResponse(newUser));
    }

    @GetMapping
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<List<UserDetailResponse>> getAll(
            @RequestParam(value = "includeBot", required = false, defaultValue = "false") boolean includeBot
    ) {
        List<User> users = userRegistratioService.getAll();

        if (!includeBot) {
            users = users.stream()
                    .filter(user -> !user.getIsBot())
                    .toList();
        }

        return ResponseEntity.ok().body(userConverter.toDetailResponseList(users));
    }

    @GetMapping("/{discordId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<UserDetailResponse> get(@PathVariable String discordId) {
        User user = userRegistratioService.get(discordId);
        return ResponseEntity.ok().body(userConverter.toDetailResponse(user));
    }

    @GetMapping("/me")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<UserDetailResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return ResponseEntity.ok(userConverter.toDetailResponse(user));
    }

    @DeleteMapping("/{discordId}")
    @RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> delete(@PathVariable @ResourceKey String discordId) {
        userRegistratioService.delete(discordId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{discordId}")
    @CheckOwner(
            service = UserRegistratioService.class,
            allowAdmin = true
    )
    public ResponseEntity<UserDetailResponse> update(@PathVariable @ResourceKey String discordId,
                                                     @Valid @RequestBody UserUpdateRequest userRequest) {
        User userUpdate = userConverter.toEntityFromUpdate(userRequest);
        userUpdate.setDiscordId(discordId);

        User existingUser = userRegistratioService.get(discordId);

        existingUser = userConverter.merge(userUpdate, existingUser);

        User updated = userRegistratioService.update(existingUser);

        return ResponseEntity.ok().body(userConverter.toDetailResponse(updated));
    }

}