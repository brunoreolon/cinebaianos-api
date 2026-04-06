package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailWithMembersResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
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

    public GroupMemberResponse toMemberResponse(GroupMember groupMember) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setMember(modelMapper.map(groupMember.getMember(), UserSummaryResponse.class));
        response.setRole(groupMember.getRole());
        response.setActive(groupMember.getActive());
        response.setSelected(groupMember.getSelected());
        response.setJoinedAt(groupMember.getJoinedAt());
        response.setLeftAt(groupMember.getLeftAt());
        return response;
    }

    public List<GroupMemberResponse> toMemberResponseList(List<GroupMember> members) {
        return members.stream()
                .map(this::toMemberResponse)
                .toList();
    }

    public GroupDetailWithMembersResponse toGroupWithMembersResponse(Group group) {
        GroupDetailWithMembersResponse response = new GroupDetailWithMembersResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setTag(group.getTag());
        response.setSlug(group.getSlug());
        response.setOwner(modelMapper.map(group.getOwner(), UserSummaryResponse.class));
        response.setActive(group.getActive());
        response.setVisibility(group.getVisibility());
        response.setJoinPolicy(group.getJoinPolicy());
        response.setOnlyAdminAddMovie(group.isOnlyAdminAddMovie());
        response.setAllowGlobalVotes(group.isAllowGlobalVotes());
        response.setVoteChangeDeadlineDays(group.getVoteChangeDeadlineDays());
        response.setMovieNewDays(group.getMovieNewDays());
        response.setInviteMaxUses(group.getInviteMaxUses());
        response.setCreatedAt(group.getCreatedAt());

        List<GroupMemberResponse> members = group.getMembers().stream()
                .filter(GroupMember::getActive)
                .map(this::toMemberResponse)
                .toList();

        response.setMembers(members);
        return response;
    }

    public Group merge(Group source, Group target) {
        modelMapper.map(source, target);
        return target;
    }

}