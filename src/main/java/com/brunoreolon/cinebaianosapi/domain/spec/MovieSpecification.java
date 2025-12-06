package com.brunoreolon.cinebaianosapi.domain.spec;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MovieSpecification {

    public static Specification<Movie> fetchGenres() {
        return (root, query, builder) -> {
            if (Movie.class.equals(query.getResultType())) {
                root.fetch("genres", JoinType.LEFT);
                query.distinct(true);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Movie> withTitle(String title) {
        if (title == null || title.isBlank()) return null;

        return (root, query, builder) ->
                builder.like(
                        builder.upper(root.get("title")),
                        "%" + title.trim().toUpperCase() + "%"
                );
    }

    public static Specification<Movie> withDateAdded(LocalDate dateAdded) {
        if (dateAdded == null) return null;

        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(
                        root.get("dateAdded").as(LocalDate.class), dateAdded
                );
    }

    public static Specification<Movie> withChooserName(String name) {
        if (name == null || name.isBlank()) return null;

        return (root, query, builder) -> {
            Join<Movie, User> chooser = root.join("chooser", JoinType.INNER);

            return builder.like(
                    builder.upper(chooser.get("name")),
                    "%" + name.trim().toUpperCase() + "%"
            );
        };
    }

}
