package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteConverter;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
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

    private final VoteService voteService;
    private final VoteConverter voteConverter;

//    @PostMapping
//    public ResponseEntity<VoteDetailResponse> register(@Valid @RequestBody VoteRequest voteRequest) {
//        Vote newVote = voteService.register(voteRequest);
//        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toModel(newVote));
//    }

//    @GetMapping
//    public ResponseEntity<List<VoteDetailResponse>> getAll() {
//        List<VoteDetailResponse> collectionModel = voteConverter.toCollectionModel(voteService.getAll());
//        return ResponseEntity.ok().body(collectionModel);
//    }

}
