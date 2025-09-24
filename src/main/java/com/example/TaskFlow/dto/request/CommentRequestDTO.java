package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotNull
    private Long taskId;

    @NotNull
    private Long authorId;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
