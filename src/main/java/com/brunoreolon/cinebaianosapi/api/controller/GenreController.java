package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GenreConverter;
import com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse;
import com.brunoreolon.cinebaianosapi.api.model.genre.stats.GenreStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.GenreVoteBreakdownResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Genre;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.service.GenreService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/genres")
@AllArgsConstructor
public class GenreController {

    private final GenreService genreService;
    private final GenreConverter genreConverter;

    @GetMapping("/rankings")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<List<GenreStatsResponse>> getGenreRankings() {
        Map<String, Integer> genreRankings = genreService.getGenreRankings();
        return ResponseEntity.ok().body(genreConverter.toResponseList(genreRankings));
    }

    @GetMapping("/vote-counts")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<List<GenreVoteBreakdownResponse>> getGenreVoteBreakdown(
            @RequestParam(name = "type", required = false) Long voteTypeId) {
        List<GenreVoteBreakdownResponse> genreVoteBreakdown = genreService.getGenreVoteBreakdown(voteTypeId);
        return ResponseEntity.ok().body(genreVoteBreakdown);
    }

    @GetMapping("/users/{discordId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<List<GenreStatsResponse>> getGenresByUser(@PathVariable String discordId) {
        Map<String, Integer> genreCount = genreService.getGenreRankingsByUser(discordId);
        return ResponseEntity.ok().body(genreConverter.toResponseList(genreCount));
    }

    @GetMapping()
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        return ResponseEntity.ok().body(genreConverter.toDetailResponseList(genres));
    }

}