package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.exception.UserNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.Email;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.util.EmailUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRegistratioService userRegistratioService;
    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final VoteService voteService;
    private final EmailUtil emailUtil;
    private final UserRepository userRepository;

    public List<Movie> getAllMovies(String discordId) {
        return userRepository.findAllMoviesByDiscordId(discordId);
    }

    public User getWithMovies(String discordId) {
        return userRepository.findByDiscordIdWithMovies(discordId)
                .filter(u -> !u.isBot())
                .orElseThrow(() -> new UserNotFoundException(String.format("User with discordId '%s' not found", discordId)));
    }

    public List<UserVoteStatsResponse> getVotesReceived(Long voteTypeId) {
        return userRegistratioService.getAll().stream()
                .filter(u -> !u.isBot())
                .map(user -> getVotesReceivedByUser(user, voteTypeId))
                .toList();
    }

    public UserVoteStatsResponse getVotesReceivedByUser(User user, Long voteType) {
        List<VoteType> votesToConsider = getVotesToConsider(voteType);

        return new UserVoteStatsResponse(
                new UserSummaryResponse(user.getDiscordId(), user.getName()),
                getVoteReceivedSummaryForUser(user, votesToConsider)
        );
    }

    public List<UserVoteStatsResponse> getVotesGiven(Long voteTypeId) {
        return userRegistratioService.getAll().stream()
                .filter(u -> !u.isBot())
                .map(user -> getVotesGivenByUser(user, voteTypeId))
                .toList();
    }

    public UserVoteStatsResponse getVotesGivenByUser(User user, Long voteType) {
        List<VoteType> votesToConsider = getVotesToConsider(voteType);

        return new UserVoteStatsResponse(
                new UserSummaryResponse(user.getDiscordId(), user.getName()),
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

        Email email = emailUtil.resetPasswordByRecover(user);
        emailService.send(email);
    }

    @Transactional
    public void resetPasswordByAdmin(String discordId, String newPassword) {
        User user = resetPassword(discordId, newPassword);

        Email email = emailUtil.resetPasswordByAdmin(user, newPassword);
        emailService.send(email);
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
    public void updateStatusAdmin(String discordId, @NotNull Boolean active) {
        User user = userRegistratioService.get(discordId);

        if (active) {
            user.AddAdmin();
        } else {
            user.RemoveAdmin();
        }
    }
}