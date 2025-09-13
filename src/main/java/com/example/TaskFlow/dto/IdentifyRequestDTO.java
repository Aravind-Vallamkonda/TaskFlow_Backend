package com.example.TaskFlow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IdentifyRequestDTO {
    @Size(min = 4, max = 50)
    private String identifier;

}
