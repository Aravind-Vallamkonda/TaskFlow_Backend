package com.example.TaskFlow.model.enums;

/**
 * Represents the permissions a team member has within the context of a team.
 * Administrators can manage membership and projects while members are limited
 * to collaborating on assigned work.
 */
public enum TeamRole {
    ADMIN,
    MEMBER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
