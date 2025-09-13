package com.example.TaskFlow.repo;

import com.example.TaskFlow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Annotation responsivle for automatic detection of repository classes
// This is a specialization of @Component
// It is used to indicate that the class provides the mechanism for storage, retrieval, search, update and delete operation on objects
// It is also used to translate database exceptions into Spring's DataAccessException hierarchy

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository is a JPA specific extension of Repository
    // It contains the full API of CrudRepository and PagingAndSortingRepository
    // It provides JPA related methods such as flushing the persistence context and delete records in a batch

    Optional<User> findByEmailOrUsername(String identifier, String identifier2);
    Optional<User> findByUsername(String username);

}
