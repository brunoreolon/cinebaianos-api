package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "group_members")
public class GroupMember implements Ownable<Long> {

    @EqualsAndHashCode.Include
    @Valid
    @EmbeddedId
    private GroupMemberId groupMemberId;

    @NotNull
    @MapsId("memberId")
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @NotNull
    @MapsId("groupId")
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GroupMemberRole role;

    private Boolean active = true;
    private Boolean selected = true;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    public Boolean canActivate() {
        return !this.getActive();
    }

    public Boolean canDisable() {
        return this.getActive();
    }

    public void select() {
        setSelected(true);
    }

    public void unselect() {
        setSelected(false);
    }

    public void activate() {
        if (!this.canActivate())
            throw new RuntimeException("Usuário já é ativo");

        this.active = true;
        this.leftAt = null;
    }

    public void disable() {
        if (!this.canDisable())
            throw new RuntimeException("Usuário já desativado");

        this.active = false;
        this.leftAt = LocalDateTime.now();
    }

    public void promoteToOwner() {
        if (getRole().canBecomeOwner()) {
            this.role = GroupMemberRole.OWNER;
        } else {
            throw new RuntimeException("Operação não permitida");
        }
    }

    public void promoteToAdmin() {
        if (getRole().canPromoteToAdmin()) {
            this.role = GroupMemberRole.ADMIN;
        } else {
            throw new RuntimeException("Operação não permitida");
        }
    }

    public void demoteToMember() {
        if (getRole().canDemoteToMember()) {
            this.role = GroupMemberRole.MEMBER;
        } else {
            throw new RuntimeException("Operação não permitida");
        }
    }

    @Override
    public Long getOwnerId() {
        return getMember().getId();
    }

//    public void revokeAdmin() {
//        if (getRole().canDemoteToMember()) {
//            this.role = GroupMemberRole.MEMBER;
//        } else {
//            throw new RuntimeException("Operação não permitida");
//        }
//    }

}