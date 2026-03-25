package com.brunoreolon.cinebaianosapi.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "group_member_bans")
public class GroupMemberBan {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User bannedUser;

    @ManyToOne
    @JoinColumn(name = "banned_by", nullable = false)
    private User bannedBy;

    private String reason;

    @CreationTimestamp()
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresdAt;

}