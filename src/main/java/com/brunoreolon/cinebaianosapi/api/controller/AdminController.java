package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.user.request.UserResetPasswordRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserStatusActiveAccountUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserStatusAdminUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeStatusUpdateRequest;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    private final UserService userService;
    private final VoteTypeService VoteTypeService;

    @PostMapping("/users/{discordId}/reset-password")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> resetPassword(@PathVariable @ResourceKey String discordId,
                                              @Valid @RequestBody UserResetPasswordRequest passwordRequest) {
        userService.resetPassword(discordId, passwordRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{discordId}/activation")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> updateStatusActiveAccount(@PathVariable String discordId,
                                             @Valid @RequestBody UserStatusActiveAccountUpdateRequest active) {
        userService.changeActivationStatus(discordId, active.getActive());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{discordId}/admin")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> updateStatusAdmin(@PathVariable String discordId,
                                             @Valid @RequestBody UserStatusAdminUpdateRequest admin) {
        userService.updateStatusAdmin(discordId, admin.getAdmin());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/vote-types/{typeVoteId}/activation")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> updateStatusVoteType(@PathVariable Long typeVoteId,
                                             @Valid @RequestBody VoteTypeStatusUpdateRequest active) {
        VoteTypeService.updateStatus(typeVoteId, active.getActive());
        return ResponseEntity.noContent().build();
    }

}
