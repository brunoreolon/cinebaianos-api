package com.brunoreolon.cinebaianosapi.core.security.authorization.enums;

public enum GroupMemberRole {

    OWNER(3) {
        @Override
        public boolean canDemoteToMember() {
            return true;
        }
    },
    ADMIN(2) {
        @Override
        public boolean canDemoteToMember() {
            return true;
        }

        @Override
        public boolean canBecomeOwner() {
            return true;
        }
    },
    MEMBER(1) {
        @Override
        public boolean canPromoteToAdmin() {
            return true;
        }
    };

    private final int level;

    GroupMemberRole(int level) {
        this.level = level;
    }

    /**
     * Retorna true se a role atual é igual ou superior à role exigida
     */
    public boolean atLeast(GroupMemberRole requiredRole) {
        return this.level >= requiredRole.level;
    }

    public boolean canPromoteToAdmin() {
        return false;
    }

    public boolean canDemoteToMember() {
        return false;
    }

    public boolean canBecomeOwner() {
        return false;
    }

}