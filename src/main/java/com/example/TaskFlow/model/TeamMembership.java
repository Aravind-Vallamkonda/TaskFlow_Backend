package com.example.TaskFlow.model;

import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "team_memberships", schema = "taskflow_app",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_member", columnNames = {"team_id", "user_id"}))
@Getter
@Setter
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TeamRole role = TeamRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MembershipStatus status = MembershipStatus.INVITED;

    @Column(name = "invited_at", nullable = false, updatable = false)
    private Instant invitedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @PrePersist
    void prePersist() {
        if (invitedAt == null) {
            invitedAt = Instant.now();
        }
    }

    public void activate() {
        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = Instant.now();
        this.respondedAt = this.joinedAt;
    }

    public void decline() {
        this.status = MembershipStatus.DECLINED;
        this.respondedAt = Instant.now();
    }

    public boolean isActiveAdmin() {
        return status == MembershipStatus.ACTIVE && role == TeamRole.ADMIN;
    }
}
