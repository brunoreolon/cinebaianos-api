package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@AllArgsConstructor
public class RankingController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<List<UserVoteStatsResponse>> getAllVotes(
            @RequestParam(name = "vote", required = false) Long voteTypeId) {
        List<UserVoteStatsResponse> votes = userService.getVotes(voteTypeId);

        return ResponseEntity.ok().body(votes);
    }

}
