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
class TeamProjectTaskRepositoryTest {

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

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistTeamProjectAndTaskGraph() {
        User owner = createUser("owner", "owner@example.com");
        User contributor = createUser("contributor", "contributor@example.com");
        owner = userRepository.save(owner);
        contributor = userRepository.save(contributor);

        Team team = Team.builder()
                .name("Engineering")
                .description("Builds the platform")
                .build();
        team.addMember(owner);
        team.addMember(contributor);

        Project project = Project.builder()
                .name("Platform")
                .description("Core services")
                .boardOrder(1)
                .build();
        team.addProject(project);

        Task task = Task.builder()
                .title("Bootstrap project")
                .description("Set up CI/CD and skeleton modules")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .columnPosition(2)
                .swimlanePosition(1)
                .build();
        task.assignTo(contributor, owner, Instant.now(), "Initial assignment");
        project.addTask(task);

        Comment comment = Comment.builder()
                .author(owner)
                .body("Kick-off meeting held")
                .build();
        task.addComment(comment);

        Attachment attachment = Attachment.builder()
                .fileName("roadmap.pdf")
                .fileType("application/pdf")
                .storageUrl("https://storage/taskflow/roadmap.pdf")
                .fileSizeBytes(2048L)
                .uploadedBy(contributor)
                .build();
        task.addAttachment(attachment);

        Team savedTeam = teamRepository.saveAndFlush(team);

        List<Team> teamsForOwner = teamRepository.findByMembers_Id(owner.getId());
        assertThat(teamsForOwner).hasSize(1);
        Team persistedTeam = teamsForOwner.get(0);
        assertThat(persistedTeam.getProjects()).hasSize(1);
        Project persistedProject = persistedTeam.getProjects().get(0);
        assertThat(persistedProject.getTasks()).hasSize(1);
        Task persistedTask = persistedProject.getTasks().get(0);
        assertThat(persistedTask.getComments()).hasSize(1);
        assertThat(persistedTask.getAttachments()).hasSize(1);
        assertThat(persistedTask.getAssignment().getAssignee().getId()).isEqualTo(contributor.getId());

        List<Project> projectsByTeam = projectRepository.findByTeamId(savedTeam.getId());
        assertThat(projectsByTeam).extracting(Project::getName).containsExactly("Platform");

        List<Task> tasksByProject = taskRepository.findByProjectId(persistedProject.getId());
        assertThat(tasksByProject).hasSize(1);
        assertThat(tasksByProject.get(0).getTitle()).isEqualTo("Bootstrap project");

        List<Task> tasksByTeam = taskRepository.findByProjectTeamId(savedTeam.getId());
        assertThat(tasksByTeam).hasSize(1);

        List<Task> tasksByAssignee = taskRepository.findByAssignment_Assignee_Id(contributor.getId());
        assertThat(tasksByAssignee).hasSize(1);
        assertThat(tasksByAssignee.get(0).getAssignment().getNotes()).isEqualTo("Initial assignment");

        assertThat(taskRepository.findByStatusOrderByColumnPositionAsc(TaskStatus.IN_PROGRESS))
                .extracting(Task::getColumnPosition)
                .containsExactly(2);

        assertThat(commentRepository.findByTaskId(persistedTask.getId()))
                .hasSize(1)
                .extracting(Comment::getBody)
                .containsExactly("Kick-off meeting held");

        assertThat(attachmentRepository.findByTaskId(persistedTask.getId()))
                .hasSize(1)
                .extracting(Attachment::getFileName)
                .containsExactly("roadmap.pdf");
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("password");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setMiddlename(null);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setIsDeleted(false);
        user.setFailedLoginAttempts(0);
        return user;
    }
}
