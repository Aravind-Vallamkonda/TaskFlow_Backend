package com.example.TaskFlow.model.enums;

/**
 * Lifecycle state of a team membership invitation.
 */
public enum MembershipStatus {
    INVITED,
    ACTIVE,
    DECLINED,
    REMOVED,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
