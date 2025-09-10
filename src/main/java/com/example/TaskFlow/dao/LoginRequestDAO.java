package com.example.TaskFlow.dao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDAO {

    @NotBlank
    @Size(min = 8, max =100)
    private String password;
    @NotBlank
    private String flowId;
}
