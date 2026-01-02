package com.brunoreolon.cinebaianosapi.api.queryFilter;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.infra.repository.spec.MovieSpecification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Filtros disponíveis para consulta de filmes")
public class MovieQueryFilter {

    @Schema(
            description = "Título (ou parte do título) do filme",
            example = "Clube da Luta"
    )
    private String title;

    @Schema(
            description = "Data em que o filme foi adicionado ao sistema",
            example = "2024-08-15",
            type = "string",
            format = "date"
    )
    private LocalDate dateAdded;

    @Schema(
            description = "Nome do usuário que escolheu o filme",
            example = "Bruno"
    )
    private String chooserName;

    @Schema(
            description = "Discord ID do usuário que escolheu o filme",
            example = "123456789012345678"
    )
    private String chooserDiscordId;

    @Schema(
            description = "Discord ID do usuário para filtrar filmes que ainda não foram votados por ele",
            example = "987654321098765432"
    )
    private String discordId;

    @Schema(
            description = "Identificador do tipo de voto aplicado ao filme",
            example = "1"
    )
    private String voteTypeId;

    @Schema(
            description = "Identificador do gênero do filme",
            example = "28"
    )
    private String genreId;

    public Specification<Movie> toSpecification() {
        return Specification
                .where(MovieSpecification.fetchGenres())
                .and(MovieSpecification.fetchVotes())
                .and(MovieSpecification.withTitle(title))
                .and(MovieSpecification.withDateAdded(dateAdded))
                .and(MovieSpecification.withChooserName(chooserName))
                .and(MovieSpecification.withChooserDiscordID(chooserDiscordId))
                .and(MovieSpecification.notVotedBy(discordId))
                .and(MovieSpecification.withVote(voteTypeId))
                .and(MovieSpecification.withGenre(genreId));
    }

}
