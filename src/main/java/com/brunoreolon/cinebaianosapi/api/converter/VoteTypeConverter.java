package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class VoteTypeConverter {

    private final ModelMapper modelMapper;

    public VoteType toEntityFromCreate(VoteTypeRequest voteTypeRequest) {
        return modelMapper.map(voteTypeRequest, VoteType.class);
    }

    public VoteType toEntityFromUpdate(VoteTypeUpdateRequest voteTypeUpdateRequest) {
        return modelMapper.map(voteTypeUpdateRequest, VoteType.class);
    }

    public VoteTypeDetailResponse toDetailResponse(VoteType voteType) {
        return modelMapper.map(voteType, VoteTypeDetailResponse.class);
    }

    public List<VoteTypeDetailResponse> toDetailResponseList(List<VoteType> voteTypes) {
        return voteTypes.stream()
                .map(this::toDetailResponse)
                .toList();
    }

}
