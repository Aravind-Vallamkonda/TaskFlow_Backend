package com.example.TaskFlow.dao;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IdentifyRequestDAO {
    @Size(min = 4, max = 50)
    private String identifier;

}
