package com.example.TaskFlow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// This class represents a User entity in the database
// It is mapped to the "users" table in the "taskflow_auth" schema
// It has unique constraints on the email and username columns
// It also has indexes on the email and username columns for faster lookups
@Entity
@Table(name = "users" ,
       //Schema which contains the table
        schema = "taskflow_auth",

       //unique constraints and indexes for email and username
       uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email",columnNames = "email"),
        @UniqueConstraint(name = "uk_users_username",columnNames = "username")
       },
       indexes = {
        @Index(name = "idx_users_email",columnList = "email"),
        @Index(name = "idx_users_username",columnList = "username")
       }
)
@Getter // Lombok annotation to generate getters for all fields
@Setter // Lombok annotation to generate setters for all fields
@EqualsAndHashCode // Lombok annotation to generate equals and hashCode methods
@ToString // Lombok annotation to generate toString method

public class User {
    @Id //Primary key for the Table
    // It is auto-incremented value. We can change it to UUID if needed by changing the strategy.
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false,length = 100)
    @Email // Belongs to the Validation Dependency
    private String email;

    @Column (unique = true,nullable = false)
    @Size(min = 4,max = 50) // Belongs to the Validation Dependency the size of the username should be between 4 and 50 characters
    // This restriction is only for the Java Variables it doesn't create an actual restriction in the Database
    private String username;

    // If the name of the column is different from the variable name we use name attribute
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, length = 32, name =  "first_name")
    @Size(min = 1, max = 32) // The size of the first name should be between 1 and 32 characters
    private String firstname;

    @Column(nullable = false, length = 32, name= "last_name")
    @Size(min = 1, max = 32) // The size of the last name should be between 1 and 32 characters
    private String lastname;

    @Column(nullable = true,length = 32, name = "middle_name")
    private String middlename;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive =false;

    //columnDefinition is used to define the default value of the column in the database
    // Here we are setting the default value of is_locked to false
    @Column (name = "is_locked", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLocked;

    @Column (name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

}
