package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movie {

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

    @NotBlank
    private String genre;

    @NotBlank
    private String year;

    @NotNull
    private String tmbdId;

    @NotBlank
    private String posterPath;

    @CreationTimestamp
    private LocalDateTime dateAdded;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Vote> votes = new ArrayList<>();

}
