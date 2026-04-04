package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.ResourceKeyValues;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRegistratioService userRegistratioService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteRepository voteRepository;

    @Value("${vote.update-limit-days}")
    private int voteUpdateLimitDays;

    public VoteService(@Lazy MovieService movieService, UserRegistratioService userRegistratioService,
                       VoteTypeRegistrationService voteTypeRegistrationService, VoteRepository voteRepository) {
        this.movieService = movieService;
        this.userRegistratioService = userRegistratioService;
        this.voteTypeRegistrationService = voteTypeRegistrationService;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public Vote register(Long userId, Long movieId, Long voteId) {
        User voter = userRegistratioService.get(userId);
        Movie movie = movieService.get(movieId);

        if (voteRepository.existsById(new VoteId(movieId, userId))) {
            throw new VoteAlreadyRegisteredException("vote.already.registered.message", new Object[]{userId, movieId});
        }

        return save(voter, movie, voteId);
    }

    @Transactional
    public Vote register(User voter, Movie movie, Long voteId) {
        return save(voter, movie, voteId);
    }

    @Transactional
    public Vote update(Long userId, Long movieId, Long voteId) {
        Vote existingVote = getVote(userId, movieId);

        long daysElapsed = ChronoUnit.DAYS.between(
                existingVote.getCreatedAt(),
                LocalDateTime.now()
        );

        if (daysElapsed > voteUpdateLimitDays) {
            throw new BusinessException(
                    "vote.modification.expired.title",
                    "vote.modification.expired.message",
                    new Object[]{daysElapsed, voteUpdateLimitDays},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        VoteType voteType = voteTypeRegistrationService.get(voteId);
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
    public void delete(Long userId, Long movieId) {
        Vote vote = getVote(userId, movieId);
        voteRepository.delete(vote);
    }

    private Vote save(User voter, Movie movie, Long voteId) {
        VoteType voteType = voteTypeRegistrationService.getOptional(voteId)
                .orElseThrow(() -> new BusinessException(
                        "vote.type.not.found.title",
                        "vote.type.not.found.message",
                        new Object[]{voteId},
                        HttpStatus.BAD_REQUEST,
                        ApiErrorCode.VOTE_TYPE_NOT_FOUND.asMap()));

        if (!voteType.isActive()) {
            throw new BusinessException(
                    "vote.type.inactive.title",
                    "vote.type.inactive.message",
                    new Object[]{voteId},
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap()
            );
        }

        Vote newVote = Vote.builder()
                .voteId(new VoteId(movie.getId(), voter.getId()))
                .movie(movie)
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