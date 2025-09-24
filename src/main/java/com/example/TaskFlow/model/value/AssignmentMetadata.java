package com.example.TaskFlow.model.value;

import com.example.TaskFlow.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Value object that encapsulates metadata about who a task is assigned to and
 * important lifecycle timestamps for the assignment.
 */
@Embeddable
@Getter
@Setter
public class AssignmentMetadata implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @JoinColumn(name = "delegate_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User delegate;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onPersist() {
        if (assignedAt == null && assignee != null) {
            assignedAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        if (acknowledgedAt != null && completedAt != null && acknowledgedAt.isAfter(completedAt)) {
            acknowledgedAt = completedAt;
        }
    }
}
