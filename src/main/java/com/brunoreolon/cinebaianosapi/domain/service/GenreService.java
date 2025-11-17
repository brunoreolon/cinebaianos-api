package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.GenreVoteBreakdownResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.GenreCountProjection;
import com.brunoreolon.cinebaianosapi.domain.repository.GenreVoteTypeCountProjection;
import com.brunoreolon.cinebaianosapi.domain.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GenreService {

    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final MovieRepository movieRepository;

    public Map<String, Integer> getGenreRankings() {
        List<GenreCountProjection> projections = movieRepository.findGenreCountsProjections();
        return projections.stream()
                .filter(p -> p.getGenre() != null)
                .collect(Collectors.toMap(
                        GenreCountProjection::getGenre,
                        GenreCountProjection::getCount,
                        Integer::sum
                ));
    }

    public Map<String, Integer> getGenreRankingsByUser(String discordId) {
        List<String> genres = movieRepository.findGenresByChooserDiscordId(discordId);

        return genres.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));
    }

    public List<GenreVoteBreakdownResponse> getGenreVoteBreakdown(Long voteTypeId) {
        // busca todos os votos agregados do repository
        List<GenreVoteTypeCountProjection> votes = movieRepository.findGenreVoteTypeCountProjection(voteTypeId);

        // cria um Map<Genre, Map<VoteType, total>> para acesso rápido
        Map<String, Map<String, Long>> votesMap = getStringMapMap(votes);

        // busca todos os gêneros e tipos de voto
        List<String> allGenres = movieRepository.findAllGenres();
        List<VoteType> allVoteTypes = getVoteTypes(voteTypeId);

        // mapeia cada gênero para o DTO
        return getGenreVoteBreakdownResponses(allGenres, allVoteTypes, votesMap);
    }

    private List<GenreVoteBreakdownResponse> getGenreVoteBreakdownResponses(List<String> allGenres, List<VoteType> allVoteTypes, Map<String, Map<String, Long>> votesMap) {
        return allGenres.stream()
                .map(genre -> {
                    List<VoteStatsResponse> voteCounts = allVoteTypes.stream()
                            .map(vt -> new VoteStatsResponse(
                                            new VoteTypeSummaryResponse(vt.getId(), vt.getName(), vt.getColor(), vt.getEmoji()),
                                            votesMap.getOrDefault(genre, Map.of()).getOrDefault(vt.getName(), 0L)
                                    )
                            )
                            // opcional: ordenar votos dentro de cada gênero pelo total decrescente
                            .sorted(Comparator.comparingLong(VoteStatsResponse::getTotalVotes).reversed())
                            .toList();

                    return new GenreVoteBreakdownResponse(genre, voteCounts);
                })
                // ordenar gêneros pelo total de votos decrescente
                .sorted(Comparator.comparingLong(sortFunction()).reversed())
                .toList();
    }

    private Map<String, Map<String, Long>> getStringMapMap(List<GenreVoteTypeCountProjection> votes) {
        return votes.stream()
                .collect(Collectors.groupingBy(
                        GenreVoteTypeCountProjection::getGenre,
                        Collectors.toMap(
                                GenreVoteTypeCountProjection::getVoteType,
                                GenreVoteTypeCountProjection::getTotal
                        )
                ));
    }

    private List<VoteType> getVoteTypes(Long voteTypeId) {
        return voteTypeId == null ? voteTypeRegistrationService.getAll(null)
                : voteTypeRegistrationService.getOptional(voteTypeId)
                .map(List::of)
                .orElse(List.of());
    }

    private ToLongFunction<GenreVoteBreakdownResponse> sortFunction() {
        return (GenreVoteBreakdownResponse g) -> g.getVotes().stream()
                .mapToLong(VoteStatsResponse::getTotalVotes)
                .sum();
    }

}
