package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByProjectTeamId(Long teamId);

    List<Task> findByAssignment_Assignee_Id(Long userId);

    List<Task> findByStatusOrderByColumnPositionAsc(TaskStatus status);
}
