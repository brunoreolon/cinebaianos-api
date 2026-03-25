package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "group_movies")
public class GroupMovie {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "chooser_id")
    private User chooser;

    @OneToMany(mappedBy = "groupMovie")
    private List<Vote> votes = new ArrayList<>();

    @CreationTimestamp()
    @Column(updatable = false, nullable = false)
    private LocalDateTime dateAdded;

}