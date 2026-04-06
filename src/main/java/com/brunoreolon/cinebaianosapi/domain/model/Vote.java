package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.Ownable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Vote implements Ownable<Long> {

    @Valid
    @EmbeddedId
    private VoteId voteId;

    @MapsId("movieId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @NotNull
    @MapsId("voterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id")
    private User voter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private VoteType vote;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Long getOwnerId() {
        return getVoter().getId();
    }

}