package com.example.TaskFlow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RegisterDTO {
    @Size(min = 4,max = 50)
    private String userName;
    @Email
    private String email;
    @Size(min = 8, max = 100)
    private String password;
}
