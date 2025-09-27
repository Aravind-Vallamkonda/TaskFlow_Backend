package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.TeamMembership;
import com.example.TaskFlow.model.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    List<TeamMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    List<TeamMembership> findByTeamIdAndStatus(Long teamId, MembershipStatus status);

    Optional<TeamMembership> findByTeam_IdAndUser_Id(Long teamId, Long userId);
}
