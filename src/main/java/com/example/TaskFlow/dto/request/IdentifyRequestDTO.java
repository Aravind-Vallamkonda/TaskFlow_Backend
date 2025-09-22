package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdentifyRequestDTO(

        @NotBlank @Size(min = 4, max = 32) String identifier){

}