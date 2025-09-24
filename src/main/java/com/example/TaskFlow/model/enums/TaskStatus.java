package com.example.TaskFlow.model.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the lifecycle states a task can move through in the Kanban workflow.
 * Each status declares the set of states it is allowed to transition to which can
 * be used by services or validators to enforce business rules.
 */
public enum TaskStatus {
    BACKLOG,
    TODO,
    IN_PROGRESS,
    BLOCKED,
    REVIEW,
    DONE,
    ARCHIVED;

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = buildTransitions();

    private static Map<TaskStatus, Set<TaskStatus>> buildTransitions() {
        Map<TaskStatus, Set<TaskStatus>> transitions = new EnumMap<>(TaskStatus.class);
        transitions.put(BACKLOG, EnumSet.of(TODO, ARCHIVED));
        transitions.put(TODO, EnumSet.of(IN_PROGRESS, BLOCKED, ARCHIVED));
        transitions.put(IN_PROGRESS, EnumSet.of(BLOCKED, REVIEW, DONE, ARCHIVED));
        transitions.put(BLOCKED, EnumSet.of(TODO, IN_PROGRESS, ARCHIVED));
        transitions.put(REVIEW, EnumSet.of(IN_PROGRESS, DONE, ARCHIVED));
        transitions.put(DONE, EnumSet.of(ARCHIVED));
        transitions.put(ARCHIVED, EnumSet.noneOf(TaskStatus.class));
        return transitions;
    }

    /**
     * Determines whether a task can move from the current status to the provided status.
     *
     * @param next the status that is being targeted
     * @return {@code true} when the transition is valid, otherwise {@code false}
     */
    public boolean canTransitionTo(TaskStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(TaskStatus.class)).contains(next);
    }
}
