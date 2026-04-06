package com.brunoreolon.cinebaianosapi.domain.model;

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

