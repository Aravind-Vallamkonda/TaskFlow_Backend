package com.example.TaskFlow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "teams", schema = "taskflow_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"members", "projects"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1024)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "team_members",
            schema = "taskflow_auth",
            joinColumns = @JoinColumn(name = "team_id", foreignKey = @ForeignKey(name = "fk_team_members_team")),
            inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_team_members_user"))
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC")
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public void addMember(User user) {
        members.add(user);
        user.getTeams().add(this);
    }

    public void removeMember(User user) {
        members.remove(user);
        user.getTeams().remove(this);
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setTeam(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setTeam(null);
    }
}
