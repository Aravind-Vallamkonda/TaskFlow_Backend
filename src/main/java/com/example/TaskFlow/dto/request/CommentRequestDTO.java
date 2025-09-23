package com.example.TaskFlow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDTO {

    private Long taskId;
    private Long authorId;
    private String body;
}
