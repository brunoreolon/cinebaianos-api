package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserMovieVoteResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class VoteConverter {

    private final ModelMapper modelMapper;

    public VoteDetailResponse toDetailResponse(Vote movie) {
        return modelMapper.map(movie, VoteDetailResponse.class);
    }

    public UserMovieVoteResponse toUserMovieVoteResponse(Vote vote) {
        return modelMapper.map(vote, UserMovieVoteResponse.class);
    }

    public List<UserMovieVoteResponse> toUserMovieVoteResponseList(List<Vote> votes) {
        return votes.stream()
                .map(this::toUserMovieVoteResponse)
                .toList();
    }
}
