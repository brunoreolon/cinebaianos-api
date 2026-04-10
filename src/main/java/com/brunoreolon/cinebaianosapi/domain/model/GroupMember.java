package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.Ownable;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;

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
            throw new BusinessException(
                    "group.member.cannot.activate.title",
                    "group.member.cannot.activate.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());

        this.active = true;
        this.leftAt = null;
    }

    public void disable() {
        if (!this.canDisable())
            throw new BusinessException(
                    "group.member.cannot.disable.title",
                    "group.member.cannot.disable.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());

        this.active = false;
        this.leftAt = LocalDateTime.now();
    }

    public void promoteToOwner() {
        if (getRole().canBecomeOwner()) {
            this.role = GroupMemberRole.OWNER;
        } else {
            throw new BusinessException(
                    "group.member.cannot.promote.to.owner.title",
                    "group.member.cannot.promote.to.owner.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());
        }
    }

    public void promoteToAdmin() {
        if (getRole().canPromoteToAdmin()) {
            this.role = GroupMemberRole.ADMIN;
        } else {
            throw new BusinessException(
                    "group.member.cannot.promote.to.admin.title",
                    "group.member.cannot.promote.to.admin.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());
        }
    }

    public void demoteToMember() {
        if (getRole().canDemoteToMember()) {
            this.role = GroupMemberRole.MEMBER;
        } else {
            throw new BusinessException(
                    "group.member.cannot.demote.to_member.title",
                    "group.member.cannot.demote.to_member.message",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.GROUP_INVALID_OPERATION.asMap());
        }
    }

    public boolean isAdmin() {
        return getRole() == GroupMemberRole.ADMIN;
    }

    public boolean isOwner() {
        return getRole() == GroupMemberRole.OWNER;
    }

    public boolean canManage() {
        return getRole().atLeast(GroupMemberRole.ADMIN);
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