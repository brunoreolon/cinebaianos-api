package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.user.request.UserUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserWithMoviesResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteSummaryResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserConverter {

    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;

    public User toEntityFromCreate(UserRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    public User toEntityFromUpdate(UserUpdateRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    public UserDetailResponse toDetailResponse(User user) {
        UserDetailResponse response = modelMapper.map(user, UserDetailResponse.class);
        response.setSuperAdmin(user.hasRole(UserRole.SUPER_ADMIN));
        response.setBanned(user.isBanned());
        return response;
    }

    public UserWithMoviesResponse toWithMoviesResponse(User user) {
        UserWithMoviesResponse map = modelMapper.map(user, UserWithMoviesResponse.class);

        if (map.getMovies() != null)
            map.getMovies().forEach(movie ->
                    movie.setPosterPath(pathUtil.fullPosterPath(movie.getPosterPath()))
            );

        return map;
    }

    public List<UserDetailResponse> toDetailResponseList(List<User> users) {
        return users.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    public User merge(User source, User target) {
        modelMapper.map(source, target);
        return target;
    }

    public UsersVotesSummaryResponse toUsersVotesSummary(Vote vote) {
        UsersVotesSummaryResponse dto = new UsersVotesSummaryResponse();

        dto.setVoter(modelMapper.map(vote.getVoter(), UserDetailResponse.class));
        dto.setVote(modelMapper.map(vote, VoteSummaryResponse.class));

        return dto;
    }

}