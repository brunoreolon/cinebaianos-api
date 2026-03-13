package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class GroupMember {

    @EmbeddedId
    @Valid
    private GroupMemberId groupMemberId;

    @NotNull
    @MapsId("memberId")
    @ManyToOne
    @JoinColumn(name = "member_id")
    private User member;

    @NotNull
    @MapsId("groupId")
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GroupRole role;

    private Boolean active = true;
    private Boolean selected = true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

}