package com.brunoreolon.cinebaianosapi.api.queryFilter;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.spec.MovieSpecification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

@Getter
@Setter
public class MovieQueryFilter {

    private String title;
    private LocalDate dateAdded;
    private String chooserName;
    private String chooserDiscordId;
    private String discordId;
    private String voteTypeId;
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
