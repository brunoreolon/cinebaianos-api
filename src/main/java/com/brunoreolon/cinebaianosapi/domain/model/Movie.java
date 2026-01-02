package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movie implements Ownable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "chooser_id")
    @NotNull
    private User chooser;

    @NotBlank
    private String title;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @OrderBy("name ASC")
    private Set<Genre> genres = new LinkedHashSet<>();

    @NotBlank
    private String year;

    @NotNull
    private String tmdbId;

    @NotBlank
    private String posterPath;

    @CreationTimestamp
    private LocalDateTime dateAdded;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Vote> votes = new LinkedHashSet<>();

    @NotBlank
    private String synopsis;

    @NotBlank
    private String director;

    @NotNull
    private Integer duration;

    @Override
    public String getOwnerId() {
        return getChooser().getDiscordId();
    }

}
