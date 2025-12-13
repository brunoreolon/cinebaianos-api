package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.ResourceKeyValues;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoteService implements OwnableService<Vote, VoteId> {

    private final MovieService movieService;
    private final UserRegistratioService userRegistratioService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteRepository voteRepository;

    public VoteService(@Lazy MovieService movieService, UserRegistratioService userRegistratioService,
                       VoteTypeRegistrationService voteTypeRegistrationService, VoteRepository voteRepository) {
        this.movieService = movieService;
        this.userRegistratioService = userRegistratioService;
        this.voteTypeRegistrationService = voteTypeRegistrationService;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public Vote register(String discordId, Long movieId, Long voteId) {
        User voter = userRegistratioService.get(discordId);
        Movie movie = movieService.get(movieId);

        if (voteRepository.existsById(new VoteId(movieId, discordId))) {
            throw new VoteAlreadyRegisteredException(String.format("Vote has already been registered for user '%s' and movie '%s'",
                    discordId, movieId));
        }

        return save(voter, movie, voteId);
    }

    @Transactional
    public Vote register(User voter, Movie movie, Long voteId) {
        return save(voter, movie, voteId);
    }

    @Transactional
    public Vote update(String discordId, Long movieId, Long voteId) {
        Vote existingVote = getVote(discordId, movieId);

        VoteType voteType = voteTypeRegistrationService.get(voteId);
        existingVote.setVote(voteType);

        return voteRepository.save(existingVote);
    }

    public Vote getVote(String discordId, Long movieId) {
        return voteRepository.findByIdWithMovie(new VoteId(movieId, discordId))
                .orElseThrow(() -> new VoteNotFoundException(String.format("Vote not found for user '%s' and movie '%s'",
                        discordId, movieId)));
    }

    public Long countVotesByTypeAndUser(VoteType voteType, User user) {
        return voteRepository.countAllByVoteTypeAndReceiver(voteType, user);
    }

    public List<Vote> getVotesByUser(String discordId) {
        return voteRepository.findByVoterWithMovie(discordId);
    }

    @Transactional
    public void delete(String discordId, Long movieId) {
        Vote vote = getVote(discordId, movieId);
        voteRepository.delete(vote);
    }

    private Vote save(User voter, Movie movie, Long voteId) {
        VoteType voteType = voteTypeRegistrationService.getOptional(voteId)
                .orElseThrow(() -> new BusinessException(
                        String.format("The vote type with id '%d' does not exist", voteId),
                        HttpStatus.BAD_REQUEST,
                        "Inactive Vote",
                        ApiErrorCode.VOTE_TYPE_NOT_FOUND.asMap()));

        if (!voteType.isActive()) {
            throw new BusinessException(
                    String.format("The vote type with id '%d' is inactive and cannot be used", voteId),
                    HttpStatus.BAD_REQUEST,
                    "Inactive Vote Type",
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap()
            );
        }

        Vote newVote = Vote.builder()
                .voteId(new VoteId(movie.getId(), voter.getDiscordId()))
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
