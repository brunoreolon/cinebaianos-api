package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.http.HttpStatus;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VoteType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotBlank
    private String description;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Color must be a valid hex code, e.g. #00FF00")
    private String color;

    private String emoji;

    private boolean active;

    public boolean canActivate() {
        return !this.isActive();
    }

    public boolean canDisable() {
        return this.isActive();
    }

    public void activate() {
        if (!this.canActivate())
            throw new BusinessException(
                    "You cannot activate a vote that is already active.",
                    HttpStatus.BAD_REQUEST,
                    "Vote cannot be activated",
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = true;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "You cannot deactivate a vote that is already deactivated.",
                    HttpStatus.BAD_REQUEST,
                    "Vote cannot be disabled",
                    ApiErrorCode.VOTE_INVALID_STATUS.asMap());

        this.active = false;
    }

}
