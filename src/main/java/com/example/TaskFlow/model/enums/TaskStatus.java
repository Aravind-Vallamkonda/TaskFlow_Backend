package com.example.TaskFlow.model.enums;

/**
 * Represents the lifecycle for a task in the TaskFlow board.
 */
public enum TaskStatus {
    BACKLOG,
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    BLOCKED,
    DONE;

    /**
     * Indicates whether the status represents work that has been completed.
     *
     * @return true if the status is a terminal state.
     */
    public boolean isTerminal() {
        return this == DONE;
    }
}
