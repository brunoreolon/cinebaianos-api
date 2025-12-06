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
    private String chooser;

    public Specification<Movie> toSpecification() {
        return Specification
                .where(MovieSpecification.fetchGenres())
                .and(MovieSpecification.withTitle(title))
                .and(MovieSpecification.withDateAdded(dateAdded))
                .and(MovieSpecification.withChooserName(chooser));
    }

}
