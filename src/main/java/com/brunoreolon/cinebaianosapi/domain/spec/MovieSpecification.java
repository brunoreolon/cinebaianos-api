package com.brunoreolon.cinebaianosapi.domain.spec;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import jakarta.persistence.criteria.*;
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

    public static Specification<Movie> fetchVotes() {
        return (root, query, builder) -> {
            if (Movie.class.equals(query.getResultType())) {
                var votes = root.fetch("votes", JoinType.LEFT);
                votes.fetch("vote", JoinType.LEFT);
                votes.fetch("voter", JoinType.LEFT);
                root.fetch("chooser", JoinType.LEFT);
                query.distinct(true);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Movie> notVotedBy(String discordId) {
        return (root, query, cb) -> {
            Subquery<Vote> subquery = query.subquery(Vote.class);
            Root<Vote> voteRoot = subquery.from(Vote.class);
            subquery.select(voteRoot);
            subquery.where(
                    cb.equal(voteRoot.get("id").get("voterId"), discordId),
                    cb.equal(voteRoot.get("id").get("movieId"), root.get("id"))
            );

            return cb.not(cb.exists(subquery));
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

    public static Specification<Movie> withChooserDiscordID(String discordId) {
        if (discordId == null || discordId.isBlank()) return null;

        return (root, query, builder) -> {
            Join<Movie, User> chooser = root.join("chooser", JoinType.INNER);
            return builder.equal(chooser.get("discordId"), discordId);
        };
    }

}
