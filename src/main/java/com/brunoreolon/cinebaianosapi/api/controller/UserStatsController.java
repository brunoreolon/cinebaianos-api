package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserStatsController {

    private final UserService userService;
    private final UserRegistratioService userRegistratioService;

    @GetMapping("/{discordId}/votes/received")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<UserVoteStatsResponse> getVotesReceivedByUser(@PathVariable String discordId,
                                                                        @RequestParam(name = "vote", required = false) Long voteType) {
        User user = userRegistratioService.get(discordId);
        UserVoteStatsResponse votesReceived = userService.getVotesReceivedByUser(user, voteType);

        return ResponseEntity.ok().body(votesReceived);
    }

    @GetMapping("/{discordId}/votes/given")
    @CheckSecurity.RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<UserVoteStatsResponse> getVotesGivenByUser(@PathVariable String discordId,
                                                                     @RequestParam(name = "vote", required = false) Long voteType) {
        User user = userRegistratioService.get(discordId);
        UserVoteStatsResponse votesGiven = userService.getVotesGivenByUser(user, voteType);

        return ResponseEntity.ok().body(votesGiven);
    }

}