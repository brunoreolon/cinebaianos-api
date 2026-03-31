package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class GroupConverter {

    private final UserConverter userConverter;
    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;

    public GroupDetailResponse toGroupWithMoviesResponse(Group group) {
        GroupDetailResponse groupResponse = modelMapper.map(group, GroupDetailResponse.class);

        List<MovieWithChooserResponse> movies = group.getMovies().stream()
                .map(gm -> toMovieResponse(gm.getMovie()))
                .toList();

        groupResponse.setMovies(movies);

        return groupResponse;
    }

    public MovieWithChooserResponse toMovieResponse(Movie movie) {
        MovieWithChooserResponse movieResponse = modelMapper.map(movie, MovieWithChooserResponse.class);
        movieResponse.setPosterPath(pathUtil.fullPosterPath(movieResponse.getPosterPath()));

        List<UsersVotesSummaryResponse> list = movie.getVotes().stream()
                .map(userConverter::toUsersVotesSummary)
                .toList();

        movieResponse.setVotes(list);

        return movieResponse;
    }

    public Group toEntiy(GroupRequest request) {
        return modelMapper.map(request, Group.class);
    }

    public Group toEntiy(GroupUpdateRequest request) {
        return modelMapper.map(request, Group.class);
    }

    public GroupResponse toResponse(Group group) {
        return modelMapper.map(group, GroupResponse.class);
    }

    public List<GroupResponse> toResponseList(List<Group> groups) {
        return groups.stream()
                .map(this::toResponse)
                .toList();
    }

}