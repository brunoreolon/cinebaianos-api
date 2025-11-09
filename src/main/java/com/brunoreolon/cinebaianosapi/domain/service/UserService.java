package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.exception.UserNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRegistratioService userRegistratioService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteService voteService;
    private final UserRepository userRepository;

    public List<Movie> getAllMovies(String discordId) {
        return userRepository.findAllMoviesByDiscordId(discordId);
    }

    public User getWithMovies(String discordId) {
        return userRepository.findByDiscordIdWithMovies(discordId)
                .filter(u -> !u.isBot())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with discordId '%s' not found", discordId)));
    }

    public List<UserVoteStatsResponse> getVotes(Long voteTypeId) {
        return userRegistratioService.getAll().stream()
                .filter(u -> !u.isBot())
                .map(user -> {
                    List<VoteType> votesToConsider = getVotesToConsider(voteTypeId);

                    return new UserVoteStatsResponse(
                            new UserSummaryResponse(user.getDiscordId(), user.getName()),
                            getVoteSummaryForUser(user, votesToConsider)
                    );
                })
                .toList();
    }

    public UserVoteStatsResponse getVotes(User user, Long voteType) {
        List<VoteType> votesToConsider = getVotesToConsider(voteType);

        return new UserVoteStatsResponse(
                new UserSummaryResponse(user.getDiscordId(), user.getName()),
                getVoteSummaryForUser(user, votesToConsider)
        );
    }

    private List<VoteType> getVotesToConsider(Long voteTypeId) {
        return (voteTypeId != null)
                ? List.of(voteTypeRegistrationService.get(voteTypeId)) :
                voteTypeRegistrationService.getAll(null);
    }

    private List<VoteStatsResponse> getVoteSummaryForUser(User user, List<VoteType> types) {
        return types.stream()
                .map(voteTypes -> {
                    Long totalVotes = voteService.countVotesByTypeAndUser(voteTypes, user);
                    VoteTypeSummaryResponse voteTypeSummaryResponse = new VoteTypeSummaryResponse(
                            voteTypes.getId(), voteTypes.getDescription(), voteTypes.getColor(), voteTypes.getEmoji()
                    );
                    return new VoteStatsResponse(voteTypeSummaryResponse, totalVotes);
                })
                .toList();
    }
}

