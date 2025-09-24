package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeamId(Long teamId);

    List<Project> findByNameContainingIgnoreCase(String name);
}
