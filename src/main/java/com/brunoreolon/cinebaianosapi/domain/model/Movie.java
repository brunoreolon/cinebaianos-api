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

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "chooser_id")
    private User chooser;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "group_id")
    private Group group;

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

    @NotNull
    private Long year;

    @NotNull
    @Column(unique = true)
    private Long tmdbId;

    private String posterPath;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
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
