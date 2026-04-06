package com.brunoreolon.cinebaianosapi.domain.model;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupPermissions {

    private boolean member;
    private GroupMemberRole role;
    private boolean canManage;
    private boolean canTransferOwnership;

}