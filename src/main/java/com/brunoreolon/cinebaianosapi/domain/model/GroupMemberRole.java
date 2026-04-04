package com.brunoreolon.cinebaianosapi.domain.model;

public enum GroupMemberRole {

    OWNER{
        @Override
        public boolean canDemoteToMember() {
            return true;
        }
    },
    ADMIN{
        @Override
        public boolean canDemoteToMember() {
            return true;
        }

        @Override
        public boolean canBecomeOwner() {
            return true;
        }
    },
    MEMBER {
        @Override
        public boolean canPromoteToAdmin() {
            return true;
        }
    };

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