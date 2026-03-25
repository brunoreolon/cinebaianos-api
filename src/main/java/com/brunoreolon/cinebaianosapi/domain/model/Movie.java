package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "movies")
public class Movie {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotNull
    private Long year;

    @NotNull
    @Column(unique = true)
    private Long tmdbId;

    private String posterPath;

    @NotBlank
    private String synopsis;

    @NotBlank
    private String director;

    @NotNull
    private Integer duration;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime dateAdded;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @OrderBy("name ASC")
    private Set<Genre> genres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "movie")
    private List<GroupMovie> groupMovies = new ArrayList<>();

}