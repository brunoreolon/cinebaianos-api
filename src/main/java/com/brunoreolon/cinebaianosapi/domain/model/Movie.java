package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
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
@Table(name = "movie")
public class Movie implements Ownable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Remover
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chooser_id")
    private User chooser;

    @NotBlank
    private String title;

    @NotBlank
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

    // Remover
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Vote> votes = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @OrderBy("name ASC")
    private Set<Genre> genres = new LinkedHashSet<>();

//    @OneToMany(mappedBy = "movie")
//    private List<GroupMovie> groupMovies = new ArrayList<>();

    @Override
    public String getOwnerId() {
        return getChooser().getDiscordId();
    }

}