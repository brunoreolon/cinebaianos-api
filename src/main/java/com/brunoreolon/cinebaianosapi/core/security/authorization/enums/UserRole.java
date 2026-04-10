package com.brunoreolon.cinebaianosapi.core.security.authorization.enums;

public enum UserRole {

    SUPER_ADMIN(2),
    USER(1);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

    /**
     * Retorna true se a role atual é igual ou superior à role exigida
     */
    public boolean atLeast(UserRole requiredUserRole) {
        return this.level >= requiredUserRole.level;
    }

}