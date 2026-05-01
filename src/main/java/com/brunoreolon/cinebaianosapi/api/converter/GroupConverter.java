package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailWithMembersResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberBanResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupPermissionsResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteSummaryResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMembershipStatus;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberBan;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMovie;
import com.brunoreolon.cinebaianosapi.domain.model.GroupPermissions;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class GroupConverter {

    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;
    private final GroupMemberService groupMemberService;

    public GroupDetailResponse toGroupWithMoviesResponse(Group group) {
        GroupDetailResponse groupResponse = modelMapper.map(group, GroupDetailResponse.class);
        applyGroupStatus(group, groupResponse);

        List<MovieWithChooserResponse> movies = group.getMovies().stream()
                .map(this::toMovieResponse)
                .toList();

        groupResponse.setMovies(movies);

        return groupResponse;
    }

    public MovieWithChooserResponse toMovieResponse(GroupMovie groupMovie) {
        Movie movie = groupMovie.getMovie();

        MovieWithChooserResponse movieResponse = new MovieWithChooserResponse();
        movieResponse.setId(movie.getId());
        movieResponse.setTitle(movie.getTitle());
        movieResponse.setYear(movie.getYear() != null ? String.valueOf(movie.getYear()) : null);
        movieResponse.setTmdbId(movie.getTmdbId() != null ? String.valueOf(movie.getTmdbId()) : null);
        movieResponse.setDateAdded(groupMovie.getDateAdded());
        movieResponse.setPosterPath(pathUtil.fullPosterPath(movie.getPosterPath()));
        movieResponse.setSynopsis(movie.getSynopsis());
        movieResponse.setDirector(movie.getDirector());
        movieResponse.setGenres(movie.getGenres().stream()
                .map(genre -> modelMapper.map(genre, com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse.class))
                .toList());
        movieResponse.setChooser(modelMapper.map(groupMovie.getChooser(), com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse.class));
        GroupMembershipStatus chooserStatus = groupMemberService.getMembershipStatus(groupMovie.getGroup().getId(), groupMovie.getChooser().getId());
        movieResponse.setChooserMembershipStatus(chooserStatus);
        movieResponse.setChooserBanExpiresAt(groupMemberService.getActiveBanExpiresAt(groupMovie.getGroup().getId(), groupMovie.getChooser().getId()));
        movieResponse.setVotes(groupMovie.getVotes().stream()
                .map(this::toUsersVotesSummaryResponse)
                .toList());

        return movieResponse;
    }

    public Group toEntiy(GroupRequest request) {
        return modelMapper.map(request, Group.class);
    }

    public Group toEntiy(GroupUpdateRequest request) {
        return modelMapper.map(request, Group.class);
    }

    public GroupResponse toResponse(Group group) {
        GroupResponse response = modelMapper.map(group, GroupResponse.class);
        applyGroupStatus(group, response);
        return response;
    }

    public GroupResponse toResponse(Group group, Integer totalMembers) {
        GroupResponse response = toResponse(group);
        response.setTotalMembers(totalMembers != null ? totalMembers : 0);

        return response;
    }

    public List<GroupResponse> toResponseList(List<Group> groups) {
        return groups.stream()
                .map(group -> toResponse(group, group.getMembers().size()))
                .toList();
    }

    public GroupMemberResponse toMemberResponse(GroupMember groupMember) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setMember(modelMapper.map(groupMember.getMember(), UserSummaryResponse.class));
        response.setRole(groupMember.getRole());
        response.setActive(groupMember.getActive());
        response.setSelected(groupMember.getSelected());
        GroupMembershipStatus membershipStatus = groupMemberService.getMembershipStatus(groupMember.getGroup().getId(), groupMember.getMember().getId());
        response.setMembershipStatus(membershipStatus);
        response.setBanned(membershipStatus == GroupMembershipStatus.BANNED_TEMPORARY || membershipStatus == GroupMembershipStatus.BANNED_PERMANENT);
        response.setBanExpiresAt(groupMemberService.getActiveBanExpiresAt(groupMember.getGroup().getId(), groupMember.getMember().getId()));
        response.setJoinedAt(groupMember.getJoinedAt());
        response.setLeftAt(groupMember.getLeftAt());
        return response;
    }

    public List<GroupMemberResponse> toMemberResponseList(List<GroupMember> members) {
        return members.stream()
                .map(this::toMemberResponse)
                .toList();
    }

    public GroupPermissionsResponse toPermissionsResponse(GroupPermissions permissions) {
        return new GroupPermissionsResponse(
                permissions.isMember(),
                permissions.getRole(),
                permissions.isCanManage(),
                permissions.isCanTransferOwnership()
        );
    }

    public GroupDetailWithMembersResponse toGroupWithMembersResponse(Group group) {
        GroupDetailWithMembersResponse response = new GroupDetailWithMembersResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setTag(group.getTag());
        response.setSlug(group.getSlug());
        response.setOwner(modelMapper.map(group.getOwner(), UserSummaryResponse.class));
        response.setActive(group.getActive());
        response.setBanned(group.isBanned());
        response.setBannedAt(group.getBannedAt());
        response.setBanReason(group.getBanReason());
        response.setExpiresAt(group.getExpiresAt());
        response.setVisibility(group.getVisibility());
        response.setJoinPolicy(group.getJoinPolicy());
        response.setOnlyAdminAddMovie(group.getOnlyAdminAddMovie());
        response.setAllowGlobalVotes(group.getAllowGlobalVotes());
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

    private UsersVotesSummaryResponse toUsersVotesSummaryResponse(Vote vote) {
        VoteSummaryResponse voteSummary = new VoteSummaryResponse(
                vote.getVote().getId(),
                vote.getVote().getDescription(),
                vote.getVote().getColor(),
                vote.getVote().getEmoji(),
                vote.getCreatedAt()
        );

        UsersVotesSummaryResponse response = new UsersVotesSummaryResponse();
        Long groupId = vote.getGroupMovie().getGroup().getId();
        Long voterId = vote.getVoter().getId();
        response.setVoter(modelMapper.map(vote.getVoter(), com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse.class));
        response.setVoterMembershipStatus(groupMemberService.getMembershipStatus(groupId, voterId));
        response.setVoterBanExpiresAt(groupMemberService.getActiveBanExpiresAt(groupId, voterId));
        response.setVote(voteSummary);
        return response;
    }

    private void applyGroupStatus(Group group, GroupResponse response) {
        response.setBanned(group.isBanned());
        response.setBannedAt(group.getBannedAt());
        response.setBanReason(group.getBanReason());
        response.setExpiresAt(group.getExpiresAt());
    }

    private void applyGroupStatus(Group group, GroupDetailResponse response) {
        response.setBanned(group.isBanned());
        response.setBannedAt(group.getBannedAt());
        response.setBanReason(group.getBanReason());
        response.setExpiresAt(group.getExpiresAt());
    }

    public GroupMemberBanResponse toBanResponse(GroupMemberBan ban) {
        GroupMemberBanResponse response = new GroupMemberBanResponse();
        response.setId(ban.getId());
        response.setMember(modelMapper.map(ban.getMember(), UserSummaryResponse.class));
        response.setBannedBy(modelMapper.map(ban.getBannedBy(), UserSummaryResponse.class));
        response.setReason(ban.getReason());
        response.setCreatedAt(ban.getCreatedAt());
        response.setExpiresAt(ban.getExpiresAt());
        return response;
    }

    public List<GroupMemberBanResponse> toBanResponseList(List<GroupMemberBan> bans) {
        return bans.stream()
                .map(this::toBanResponse)
                .toList();
    }

    public Group merge(Group source, Group target) {
        modelMapper.map(source, target);
        return target;
    }

}