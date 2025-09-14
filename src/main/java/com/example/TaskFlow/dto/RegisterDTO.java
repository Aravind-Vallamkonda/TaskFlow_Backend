package com.example.TaskFlow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    @Size(min = 4,max = 50)
    private String username;
    @Email
    @NotBlank
    private String email;
    @Size(min = 8, max = 100)
    @NotBlank
    private String password;
    @NotBlank
    @Size(min =1 , max =32)
    private String firstname;
    @NotBlank
    @Size(min = 1,max=32)
    private String lastname;

}
