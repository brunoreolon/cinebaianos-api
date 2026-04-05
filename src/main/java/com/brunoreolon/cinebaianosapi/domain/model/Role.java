package com.brunoreolon.cinebaianosapi.domain.model;

public enum Role {

    SUPER_ADMIN(3),
    ADMIN(2),
    USER(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    /**
     * Retorna true se a role atual é igual ou superior à role exigida
     */
    public boolean atLeast(Role requiredRole) {
        return this.level >= requiredRole.level;
    }

}