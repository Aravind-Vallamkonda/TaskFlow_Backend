package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdOrderBySortOrderAsc(Long projectId);

    List<Task> findByAssignmentAssigneeId(Long assigneeId);

    List<Task> findByStatus(TaskStatus status);
}
