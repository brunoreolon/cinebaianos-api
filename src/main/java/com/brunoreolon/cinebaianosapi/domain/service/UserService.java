package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserStats;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole;
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

import java.time.LocalDateTime;
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

    public List<Movie> getAllMovies(Long userId) {
        return userRepository.findAllMoviesById(userId);
    }

    public User getWithMovies(Long userId) {
        return userRepository.findByIdWithMovies(userId)
                .filter(u -> !u.getIsBot())
                .orElseThrow(() -> new UserNotFoundException("user.not.found.message", new Object[]{userId}));
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
                        user.getId(),
                        user.getDiscordId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getBiography(),
                        user.getCreatedAt(),
                        user.isAdmin(),
                        user.hasRole(UserRole.SUPER_ADMIN),
                        user.getIsBot(),
                        user.getActive(),
                        user.isBanned()
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
                        user.getId(),
                        user.getDiscordId(),
                        user.getName(),
                        user.getEmail(),
                        user.getAvatar(),
                        user.getBiography(),
                        user.getCreatedAt(),
                        user.isAdmin(),
                        user.hasRole(UserRole.SUPER_ADMIN),
                        user.getIsBot(),
                        user.getActive(),
                        user.isBanned()
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

    private User resetPassword(Long userId, String newPassword) {
        User user = userRegistratioService.get(userId);
        user.setPassword(passwordEncoder.encode(newPassword));

        return user;
    }

    @Transactional
    public void resetPasswordByRecover(Long userId, String newPassword) {
        User user = resetPassword(userId, newPassword);
        publisher.publishEvent(new PasswordResetByRecoverEvent(user));
    }

    @Transactional
    public void resetPasswordByAdmin(Long userId, String newPassword) {
        User user = resetPassword(userId, newPassword);
        publisher.publishEvent(new PasswordResetByAdminEvent(user, newPassword));
    }

    @Transactional
    public void changeActivationStatus(Long userId, boolean active) {
        User user = userRegistratioService.get(userId);

        if (active) {
            user.activate();
        } else {
            user.disable();
        }
    }

    @Transactional
    public void updateStatusAdmin(String loggedUserIdentifier, Long targetUserId, Boolean active) {
        User loggedUser = userRepository.findByEmail(loggedUserIdentifier)
                .orElseThrow(() -> new IllegalStateException("Logged user not found"));

        if (loggedUser.getId().equals(targetUserId)) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.cannot_remove_own_admin.message",
                    HttpStatus.FORBIDDEN
            );
        }

        User user = userRegistratioService.get(targetUserId);

        if (active) {
            user.AddAdmin();
        } else {
            user.RemoveAdmin();
        }
    }

    public com.brunoreolon.cinebaianosapi.api.model.user.stats.UserSummaryResponse getUserSummary(Long userId) {
        User user = userRegistratioService.get(userId);

        UserDetailResponse userDetail = new UserDetailResponse(
                user.getId(),
                user.getDiscordId(),
                user.getName(),
                user.getEmail(),
                user.getAvatar(),
                user.getBiography(),
                user.getCreatedAt(),
                user.isAdmin(),
                user.hasRole(UserRole.SUPER_ADMIN),
                user.getIsBot(),
                user.getActive(),
                user.isBanned()
        );

        UserSummaryProjection summary = null;
//        UserSummaryProjection summary = userStatsRepository.findUserSummaryById(userId);

        return new com.brunoreolon.cinebaianosapi.api.model.user.stats.UserSummaryResponse(
                userDetail,
                null
//                new UserStats(
//                        summary.getTotalMoviesAdded(),
//                        summary.getTotalVotesGiven(),
//                        summary.getTotalVotesReceived(),
//                        summary.getMoviesPendingVote()
//                )
        );
    }

    @Transactional
    public void banUser(Long targetUserId, Long bannedById, String reason, LocalDateTime expiresAt) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.ban.reason.required.message",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.ban.expires.invalid.message",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (targetUserId.equals(bannedById)) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.ban.self.not.allowed.message",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        User target = userRegistratioService.get(targetUserId);
        User bannedBy = userRegistratioService.get(bannedById);

        if (target.isBanned()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.already.banned.message",
                    new Object[]{targetUserId},
                    HttpStatus.CONFLICT
            );
        }

        target.ban(bannedBy, reason.trim(), expiresAt);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRegistratioService.get(userId);

        if (!user.isBanned()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "user.not.banned.message",
                    new Object[]{userId},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        user.unban();
    }

}