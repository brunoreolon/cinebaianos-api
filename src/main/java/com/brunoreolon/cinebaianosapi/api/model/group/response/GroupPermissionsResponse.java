package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupPermissionsResponse {

    private boolean member;
    private GroupMemberRole role;
    private boolean canManage;
    private boolean canTransferOwnership;

}