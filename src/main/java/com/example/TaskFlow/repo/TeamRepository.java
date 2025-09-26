package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    List<Team> findDistinctByMembershipsUserIdAndMembershipsStatus(Long memberId, MembershipStatus status);

    Optional<Team> findByIdAndMembershipsUserId(Long teamId, Long memberId);
}
