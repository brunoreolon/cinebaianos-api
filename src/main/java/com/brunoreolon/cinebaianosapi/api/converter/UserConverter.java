package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.user.request.UserUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserWithMoviesResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserConverter {

    private final ModelMapper modelMapper;

    public User toEntityFromCreate(UserRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    public User toEntityFromUpdate(UserUpdateRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    public UserDetailResponse toDetailResponse(User user) {
        return modelMapper.map(user, UserDetailResponse.class);
    }

    public UserWithMoviesResponse toWithMoviesResponse(User user) {
        return modelMapper.map(user, UserWithMoviesResponse.class);
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

}
