package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteConverter;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserMovieVoteResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
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

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@AllArgsConstructor
public class VoteController {

    private final UserService userService;
    private final UserRegistratioService userRegistratioService;
    private final VoteService voteService;
    private final VoteConverter voteConverter;

    @GetMapping("/rankings")
    public ResponseEntity<List<UserVoteStatsResponse>> getVoteRankings(
            @RequestParam(name = "type", required = false) Long voteTypeId) {
        List<UserVoteStatsResponse> votes = userService.getVotes(voteTypeId);

        return ResponseEntity.ok().body(votes);
    }

    @GetMapping("/users/{discordId}")
    public ResponseEntity<UserVoteStatsResponse> getUserVotes(@RequestParam(name = "vote", required = false) Long voteType,
                                                              @PathVariable String discordId) {
        User user = userRegistratioService.get(discordId);
        UserVoteStatsResponse votes = userService.getVotes(user, voteType);

        return ResponseEntity.ok().body(votes);
    }

    @GetMapping("/users/{discordId}/movies-votes")
    public ResponseEntity<List<UserMovieVoteResponse>> getUserMovieVotes(@PathVariable String discordId) {
        List<Vote> votes = voteService.getVotesByUser(discordId);
        List<UserMovieVoteResponse> response = voteConverter.toUserMovieVoteResponseList(votes);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping()
    public ResponseEntity<VoteDetailResponse> registerVote(@PathVariable String discordId,
                                                           @Valid @RequestBody VoteRequest voteRequest) {
        Vote newVote = voteService.register(discordId, voteRequest.getMovie().getId(), voteRequest.getVote());
        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toDetailResponse(newVote));
    }

    @PutMapping("/users/{discordId}/movies/{movieId}")
    public ResponseEntity<VoteDetailResponse> updateVote(@PathVariable String discordId,
                                                         @PathVariable Long movieId,
                                                         @Valid @RequestBody VoteTypeId voteTypeId) {
        Vote newVote = voteService.update(discordId, movieId, voteTypeId.getId());
        return ResponseEntity.ok().body(voteConverter.toDetailResponse(newVote));
    }

    @DeleteMapping("/users/{discordId}/movies/{movieId}")
    public ResponseEntity<Void> deleteVote(@PathVariable String discordId,
                                           @PathVariable Long movieId) {
        voteService.delete(discordId, movieId);
        return ResponseEntity.noContent().build();
    }

}
