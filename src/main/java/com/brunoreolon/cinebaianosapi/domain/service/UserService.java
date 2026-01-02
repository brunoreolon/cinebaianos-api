package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserStats;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.event.PasswordResetByAdminEvent;
import com.brunoreolon.cinebaianosapi.domain.event.PasswordResetByRecoverEvent;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.UserNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.UserStatsRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.UserSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRegistratioService userRegistratioService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final PasswordEncoder passwordEncoder;
    private final VoteService voteService;
    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public List<Movie> getAllMovies(String discordId) {
        return userRepository.findAllMoviesByDiscordId(discordId);
    }

    public User getWithMovies(String discordId) {
        return userRepository.findByDiscordIdWithMovies(discordId)
                .filter(u -> !u.getIsBot())
                .orElseThrow(() -> new UserNotFoundException("user.not.found.message", new Object[]{discordId}));
    }

    public List<UserVoteStatsResponse> getVotesReceived(Long voteTypeId) {
        return userRegistratioService.getAll().stream()
                .filter(u -> !u.getIsBot())
                .map(user -> getVotesReceivedByUser(user, voteTypeId))
                .toList();
    }

    public UserVoteStatsResponse getVotesReceivedByUser(User user, Long voteType) {
        List<VoteType> votesToConsider = getVotesToConsider(voteType);

        return new UserVoteStatsResponse(
                new UserDetailResponse(
                        user.getDiscordId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getBiography(),
                        user.getCreated(),
                        user.isAdmin(),
                        user.getIsBot(),
                        user.getActive()
                ),
                getVoteReceivedSummaryForUser(user, votesToConsider)
        );
    }

    public List<UserVoteStatsResponse> getVotesGiven(Long voteTypeId) {
        return userRegistratioService.getAll().stream()
                .filter(u -> !u.getIsBot())
                .map(user -> getVotesGivenByUser(user, voteTypeId))
                .toList();
    }

    public UserVoteStatsResponse getVotesGivenByUser(User user, Long voteType) {
        List<VoteType> votesToConsider = getVotesToConsider(voteType);

        return new UserVoteStatsResponse(
                new UserDetailResponse(
                        user.getDiscordId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getBiography(),
                        user.getCreated(),
                        user.isAdmin(),
                        user.getIsBot(),
                        user.getActive()
                ),
                getVoteGivenSummaryForUser(user, votesToConsider)
        );
    }

    private List<VoteType> getVotesToConsider(Long voteTypeId) {
        return (voteTypeId != null)
                ? List.of(voteTypeRegistrationService.get(voteTypeId)) :
                voteTypeRegistrationService.getAll(null);
    }

    private List<VoteStatsResponse> getVoteReceivedSummaryForUser(User user, List<VoteType> types) {
        return types.stream()
                .map(voteTypes -> {
                    Long totalVotes = voteService.countVotesReceivedByTypeForUser(voteTypes, user);
                    VoteTypeSummaryResponse voteTypeSummaryResponse = new VoteTypeSummaryResponse(
                            voteTypes.getId(), voteTypes.getDescription(), voteTypes.getColor(), voteTypes.getEmoji()
                    );
                    return new VoteStatsResponse(voteTypeSummaryResponse, totalVotes);
                })
                .toList();
    }

    private List<VoteStatsResponse> getVoteGivenSummaryForUser(User user, List<VoteType> types) {
        return types.stream()
                .map(voteTypes -> {
                    Long totalVotes = voteService.countVotesGivenByTypeForUser(voteTypes, user);
                    VoteTypeSummaryResponse voteTypeSummaryResponse = new VoteTypeSummaryResponse(
                            voteTypes.getId(), voteTypes.getDescription(), voteTypes.getColor(), voteTypes.getEmoji()
                    );
                    return new VoteStatsResponse(voteTypeSummaryResponse, totalVotes);
                })
                .toList();
    }

    private User resetPassword(String discordId, String newPassword) {
        User user = userRegistratioService.get(discordId);
        user.setPassword(passwordEncoder.encode(newPassword));

        return user;
    }

    @Transactional
    public void resetPasswordByRecover(String discordId, String newPassword) {
        User user = resetPassword(discordId, newPassword);
        publisher.publishEvent(new PasswordResetByRecoverEvent(user));
    }

    @Transactional
    public void resetPasswordByAdmin(String discordId, String newPassword) {
        User user = resetPassword(discordId, newPassword);
        publisher.publishEvent(new PasswordResetByAdminEvent(user, newPassword));
    }

    @Transactional
    public void changeActivationStatus(String discordId, boolean active) {
        User user = userRegistratioService.get(discordId);

        if (active) {
            user.activate();
        } else {
            user.disable();
        }
    }

    @Transactional
    public void updateStatusAdmin(String loggedUserIdentifier, String targetDiscordId, Boolean active) {
        User loggedUser = userRepository.findByEmail(loggedUserIdentifier)
                .orElseThrow(() -> new IllegalStateException("Logged user not found"));

        if (loggedUser.getDiscordId().equals(targetDiscordId)) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.cannot_remove_own_admin.message",
                    HttpStatus.FORBIDDEN
            );
        }

        User user = userRegistratioService.get(targetDiscordId);

        if (active) {
            user.AddAdmin();
        } else {
            user.RemoveAdmin();
        }
    }

    public com.brunoreolon.cinebaianosapi.api.model.user.stats.UserSummaryResponse getUserSummary(String discordId) {
        User user = userRegistratioService.get(discordId);

        UserDetailResponse userDetail = new UserDetailResponse(
                user.getDiscordId(),
                user.getName(),
                user.getEmail(),
                user.getAvatar(),
                user.getBiography(),
                user.getCreated(),
                user.isAdmin(),
                user.getIsBot(),
                user.getActive()
        );

        UserSummaryProjection summary = userStatsRepository.findUserSummaryByDiscordId(discordId);

        return new com.brunoreolon.cinebaianosapi.api.model.user.stats.UserSummaryResponse(
                userDetail,
                new UserStats(
                        summary.getTotalMoviesAdded(),
                        summary.getTotalVotesGiven(),
                        summary.getTotalVotesReceived(),
                        summary.getMoviesPendingVote()
                )
        );
    }

}