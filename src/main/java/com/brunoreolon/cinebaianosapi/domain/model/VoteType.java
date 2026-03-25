package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class VoteType {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Size(max = 10)
    private String emoji;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex code, e.g. #00FF00")
    @Size(max = 7)
    private String color;

    private boolean active;
//    private Boolean active = true;

    @CreationTimestamp()
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime created;

    public boolean canActivate() {
        return !this.isActive();
    }

    public boolean canDisable() {
        return this.isActive();
    }

    public void activate() {
        if (!this.canActivate())
            throw new BusinessException(
                    "vote.type.cannot.activate.title",
                    "vote.type.cannot.activate.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "vote.type.cannot.disable.title",
                    "vote.type.cannot.disable.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = false;
    }

}