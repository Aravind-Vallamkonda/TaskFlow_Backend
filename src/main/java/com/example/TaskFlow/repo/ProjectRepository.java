package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByTeamId(Long teamId);

    Optional<Project> findByTeamIdAndNameIgnoreCase(Long teamId, String name);
}
