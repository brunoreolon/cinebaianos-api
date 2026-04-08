package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.ResourceKeyValues;
import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteTypeNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupMemberRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupMovieRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VoteService implements OwnableService<Vote, VoteId> {

    private final MovieService movieService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteRepository voteRepository;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMovieRepository groupMovieRepository;

    public VoteService(@Lazy MovieService movieService, VoteTypeRegistrationService voteTypeRegistrationService,
                       VoteRepository voteRepository, @Lazy GroupService groupService,
                       GroupMemberRepository groupMemberRepository, GroupMovieRepository groupMovieRepository) {
        this.movieService = movieService;
        this.voteTypeRegistrationService = voteTypeRegistrationService;
        this.voteRepository = voteRepository;
        this.groupService = groupService;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMovieRepository = groupMovieRepository;
    }

    @Transactional
    public Vote registerByGroup(Long userId, Long groupId, Long movieId, Long voteId) {
        Group group = groupService.get(groupId);
        VoteType voteType = getVoteType(voteId);
        GroupMember voter = getGroupMember(userId, group);

        validateVoteTypeIsValidForGroup(group, voteType);
        validateGlobalVoteTypeAllowedForGroup(voteId, group, voteType);

        GroupMovie groupMovie = getGroupMovie(movieId, group);

        boolean voteExists = voteRepository.existsByGroupMovieAndVoterId(groupMovie, userId);
        if (voteExists) throw new VoteAlreadyRegisteredException("vote.already.registered.message", new Object[]{userId, movieId});

        return saveByGroup(voter.getMember(), groupMovie, voteType);
    }

    private void validateVoteTypeIsValidForGroup(Group group, VoteType voteType) {
        if (!voteType.isGlobal() && !voteType.getGroup().getId().equals(group.getId())) {
            throw new BusinessException(
                    "vote.type.not.valid.for.group.title",
                    "vote.type.not.valid.for.group.message",
                    new Object[]{voteType.getId(), group.getId()},
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_TYPE_NOT_FOUND.asMap()
            );
        }
    }

    @Transactional
    public Vote updateByGroup(Long userId, Long groupId, Long movieId, Long voteId) {
        Group group = groupService.get(groupId);
        GroupMovie groupMovie = getGroupMovie(movieId, group);
        Vote existingVote = getVote(userId, movieId, groupMovie);
        VoteType voteType = voteTypeRegistrationService.get(voteId);

        validateVoteTypeIsValidForGroup(group, voteType);
        validateGlobalVoteTypeAllowedForGroup(voteId, group, voteType);

        long daysElapsed = getDaysElapsed(existingVote);
        validateDaysElapsed(daysElapsed, group.getVoteChangeDeadlineDays());

        existingVote.setVote(voteType);

        return voteRepository.save(existingVote);
    }

    public Vote getVote(Long userId, Long movieId) {
        return voteRepository.findByIdWithMovieAndVoter(new VoteId(movieId, userId))
                .orElseThrow(() -> new VoteNotFoundException("vote.not.found.message", new Object[]{userId, movieId}));
    }

    public Long countVotesReceivedByTypeForUser(VoteType voteType, User user) {
        return voteRepository.countAllByVoteTypeAndReceiver(voteType, user);
    }

    public Long countVotesGivenByTypeForUser(VoteType voteType, User user) {
        return voteRepository.countAllByVoteTypeAndGiver(voteType, user);
    }

    public List<Vote> getVotesByUser(Long userId) {
        return voteRepository.findByVoterWithMovie(userId);
    }

    @Transactional
    public void deleteByGroup(Long userId, Long groupId, Long movieId) {
        Group group = groupService.get(groupId);
        GroupMovie groupMovie = getGroupMovie(movieId, group);

        Vote vote = getVote(userId, movieId, groupMovie);

        voteRepository.delete(vote);
    }

    private GroupMember getGroupMember(Long userId, Group group) {
        return groupMemberRepository.findByGroupIdAndMemberId(group.getId(), userId)
                .orElseThrow(() -> new BusinessException(
                        "group.member.not.found.title",
                        "group.member.not.found.message",
                        new Object[]{userId, group.getId()},
                        HttpStatus.BAD_REQUEST
                ));
    }

    private void validateGlobalVoteTypeAllowedForGroup(Long voteId, Group group, VoteType voteType) {
        Boolean allowGlobalVotes = group.getAllowGlobalVotes();

        if (Boolean.FALSE.equals(allowGlobalVotes) && voteType.isGlobal()) {
            throw new BusinessException(
                    "vote.type.global.not.allowed.title",
                    "vote.type.global.not.allowed.message",
                    new Object[]{voteId, group.getId()},
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap()
            );
        }
    }

    private long getDaysElapsed(Vote existingVote) {
        return ChronoUnit.DAYS.between(
                existingVote.getCreatedAt(),
                LocalDateTime.now()
        );
    }

    private void validateDaysElapsed(long daysElapsed, long voteChangeDeadlineDays) {
        if (daysElapsed > voteChangeDeadlineDays) {
            throw new BusinessException(
                    "vote.modification.expired.title",
                    "vote.modification.expired.message",
                    new Object[]{daysElapsed, voteChangeDeadlineDays},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    private Vote getVote(Long userId, Long movieId, GroupMovie groupMovie) {
        return voteRepository.findByGroupMovieAndVoterId(groupMovie, userId)
                .orElseThrow(() -> new VoteNotFoundException(
                        "vote.not.found.message",
                        new Object[]{userId, movieId},
                        HttpStatus.BAD_REQUEST,
                        ApiErrorCode.VOTE_TYPE_NOT_FOUND
                ));
    }

    private GroupMovie getGroupMovie(Long movieId, Group group) {
        return groupMovieRepository.findByGroupIdAndMovieId(group.getId(), movieId)
                .orElseThrow(() -> new BusinessException(
                        "group.movie.not.found.title",
                        "group.movie.not.found.message",
                        new Object[]{group.getId(), movieId},
                        HttpStatus.NOT_FOUND
                ));
    }

    private VoteType getVoteType(Long voteId) {
        return voteTypeRegistrationService.getOptional(voteId)
                .filter(VoteType::isActive)
                .orElseThrow(() -> new VoteTypeNotFoundException(
                        "vote.type.not.found.message",
                        new Object[]{voteId},
                        HttpStatus.BAD_REQUEST,
                        ApiErrorCode.VOTE_TYPE_NOT_FOUND));
    }

    private Vote saveByGroup(User voter, GroupMovie groupMovie, VoteType voteType) {
        if (!voteType.isActive()) {
            throw new BusinessException(
                    "vote.type.inactive.title",
                    "vote.type.inactive.message",
                    new Object[]{voteType.getId()},
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap()
            );
        }

        Vote newVote = Vote.builder()
                .groupMovie(groupMovie)
                .voter(voter)
                .vote(voteType)
                .build();

        return voteRepository.save(newVote);
    }

    public MovieVotes getMovieVotesReceived(Long movieId) {
        Movie movie = movieService.get(movieId);
        List<Vote> votes = voteRepository.findByMovieId(movieId);

        return new MovieVotes(movie, votes);
    }

    @Override
    public Vote get(VoteId key) {
        return getVote(key.getVoterId(), key.getMovieId());
    }

    @Override
    public VoteId buildId(ResourceKeyValues keyValues) {
        return keyValues.as(VoteId.class);
    }

}