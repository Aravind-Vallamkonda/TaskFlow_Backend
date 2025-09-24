package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Attachment;
import com.example.TaskFlow.model.Comment;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamProjectTaskRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    void shouldPersistProjectWithTaskHierarchy() {
        User owner = persistUser("owner");

        Team team = new Team();
        team.setName("Platform");
        team.setDescription("Core platform initiatives");
        team.addMember(owner);
        team = teamRepository.saveAndFlush(team);

        Project project = new Project();
        project.setName("Service Mesh");
        project.setDescription("Roll out mesh for internal services");
        project.setStartDate(LocalDate.now());
        project.setDueDate(LocalDate.now().plusDays(30));
        team.addProject(project);

        Task task = new Task();
        task.setTitle("Design rollout plan");
        task.setDescription("Prepare RFC covering rollout phases");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(14));
        task.setSortOrder(5);
        task.getAssignment().setAssignee(owner);
        task.getAssignment().setAssignedAt(Instant.now());

        Comment comment = new Comment();
        comment.setAuthor(owner);
        comment.setTask(task);
        comment.setContent("Kick-off scheduled with stakeholders.");
        task.getComments().add(comment);

        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName("rollout-plan.pdf");
        attachment.setContentType("application/pdf");
        attachment.setStorageUrl("https://files.local/rollout-plan.pdf");
        attachment.setFileSize(2048L);
        task.getAttachments().add(attachment);

        project.addTask(task);
        projectRepository.saveAndFlush(project);

        List<Project> persistedProjects = projectRepository.findByTeamId(team.getId());
        assertThat(persistedProjects).hasSize(1);
        assertThat(persistedProjects.get(0).getTasks()).hasSize(1);

        List<Task> persistedTasks = taskRepository.findByProjectIdOrderBySortOrderAsc(project.getId());
        assertThat(persistedTasks).hasSize(1);
        Task persistedTask = persistedTasks.get(0);
        assertThat(persistedTask.getAssignment().getAssignee()).isNotNull();
        assertThat(commentRepository.findByTaskIdOrderByCreatedAtAsc(persistedTask.getId()))
                .hasSize(1)
                .allMatch(stored -> stored.getContent().contains("Kick-off"));
        assertThat(attachmentRepository.findByTaskId(persistedTask.getId()))
                .hasSize(1)
                .extracting(Attachment::getFileName)
                .contains("rollout-plan.pdf");
    }

    @Test
    void shouldFindTeamsAndTasksByMembership() {
        User primary = persistUser("primary");
        User collaborator = persistUser("collab");

        Team team = new Team();
        team.setName("Mobile");
        team.addMember(primary);
        team.addMember(collaborator);
        team = teamRepository.saveAndFlush(team);

        Project project = new Project();
        project.setName("Mobile App");
        project.setDescription("Rebuild onboarding flow");
        team.addProject(project);

        Task task = new Task();
        task.setTitle("Implement biometric login");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.MEDIUM);
        task.setSortOrder(1);
        task.getAssignment().setAssignee(collaborator);
        task.getAssignment().setAssignedAt(Instant.now());
        project.addTask(task);

        projectRepository.saveAndFlush(project);

        assertThat(teamRepository.findByMembersId(primary.getId()))
                .extracting(Team::getId)
                .contains(team.getId());

        assertThat(taskRepository.findByAssignmentAssigneeId(collaborator.getId()))
                .extracting(Task::getTitle)
                .contains("Implement biometric login");
    }

    private User persistUser(String seed) {
        User user = new User();
        user.setEmail(seed + "@example.com");
        user.setUsername(seed);
        user.setPasswordHash("secret");
        user.setFirstname("First");
        user.setLastname("User");
        user.setIsActive(true);
        return userRepository.saveAndFlush(user);
    }
}
