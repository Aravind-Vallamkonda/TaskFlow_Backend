package com.example.TaskFlow.model;

import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "teams", schema = "taskflow_app",
        uniqueConstraints = {@UniqueConstraint(name = "uk_team_name", columnNames = "name")})
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TeamMembership> memberships = new java.util.HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Project> projects = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setTeam(this);
    }

    public TeamMembership addMembership(User user, TeamRole role) {
        TeamMembership membership = new TeamMembership();
        membership.setTeam(this);
        membership.setUser(user);
        membership.setRole(role);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.activate();
        memberships.add(membership);
        user.getMemberships().add(membership);
        return membership;
    }

    public Set<User> getActiveMembers() {
        return memberships.stream()
                .filter(m -> m.getStatus().isActive())
                .map(TeamMembership::getUser)
                .collect(Collectors.toSet());
    }
}
