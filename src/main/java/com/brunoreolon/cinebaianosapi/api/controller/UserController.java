package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteConverter;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserWithMoviesResponse;
import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRegistratioService userRegistratioService;
    private final VoteService voteService;
    private final UserConverter userConverter;
    private final VoteConverter voteConverter;

    @GetMapping("/{discordId}/movies")
    public ResponseEntity<UserWithMoviesResponse> getMovies(@PathVariable String discordId) {
        User user = userService.getWithMovies(discordId);
        UserWithMoviesResponse response = userConverter.toWithMoviesResponse(user);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{discordId}/votes")
    public ResponseEntity<UserVoteStatsResponse> getVotes(@RequestParam(name = "vote", required = false) Long voteType,
                                                          @PathVariable String discordId) {
        User user = userRegistratioService.get(discordId);
        UserVoteStatsResponse votes = userService.getVotes(user, voteType);

        return ResponseEntity.ok().body(votes);
    }

    @PostMapping("/{discordId}/votes")
    public ResponseEntity<VoteDetailResponse> registerVote(@PathVariable String discordId,
                                                           @Valid @RequestBody VoteRequest voteRequest) {
        Vote newVote = voteService.register(discordId, voteRequest.getMovie().getId(), voteRequest.getVote());
        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toDetailResponse(newVote));
    }

    @PutMapping("/{discordId}/votes")
    public ResponseEntity<VoteDetailResponse> update(@PathVariable String discordId,
                                                           @Valid @RequestBody VoteRequest voteRequest) {
        Vote newVote = voteService.update(discordId, voteRequest.getMovie().getId(), voteRequest.getVote());
        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toDetailResponse(newVote));
    }

}
