package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.Size;

public record IdentifyRequestDTO(
    @Size(min = 4, max = 32) String identifier){

}