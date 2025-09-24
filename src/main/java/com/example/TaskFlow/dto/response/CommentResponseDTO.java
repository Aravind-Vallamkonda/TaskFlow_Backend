package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CommentResponseDTO {
    Long id;
    Long taskId;
    Long authorId;
    String content;
    Instant createdAt;
    Instant updatedAt;
}
