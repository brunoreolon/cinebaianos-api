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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

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

    @Transactional
    public void resetPassword(String discordId, String newPassword) {
        User user = userRegistratioService.get(discordId);
        user.setPassword(passwordEncoder.encode(newPassword));

        String assunto = "Senha redefinida pelo administrador - Cinebaianos";
        String conteudo = "<html><body>"
                + "<p>Olá, <b>" + user.getName() + "</b>!</p>"
                + "<p>A senha da sua conta no <b>Cinebaianos</b> foi redefinida por um administrador do sistema.</p>"
                + "<p>Sua nova senha temporária é:</p>"
                + "<p><b>" + newPassword + "</b></p>"
                + "<p>Por segurança, recomendamos que você faça login e altere essa senha assim que possível.</p>"
                + "<p>Você pode acessar a plataforma pelo link abaixo:</p>"
                + "<p><a href='" + frontendUrl + "'>" + frontendUrl + "</a></p>"
                + "<p>Se você não reconhece essa ação, entre em contato com o suporte.</p>"
                + "<p>Atenciosamente,<br>Equipe Cinebaianos</p>"
                + "</body></html>";

        emailService.send(user.getEmail(), newPassword, assunto, conteudo);
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

