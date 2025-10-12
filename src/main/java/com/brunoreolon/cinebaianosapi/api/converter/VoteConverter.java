package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VoteConverter {

    private final ModelMapper modelMapper;

    public VoteDetailResponse toDetailResponse(Vote movie) {
        return modelMapper.map(movie, VoteDetailResponse.class);
    }

}
