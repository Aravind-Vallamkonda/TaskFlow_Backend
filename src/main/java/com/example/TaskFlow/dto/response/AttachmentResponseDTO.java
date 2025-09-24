package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AttachmentResponseDTO {
    Long id;
    Long taskId;
    String fileName;
    String contentType;
    String storageUrl;
    Long fileSize;
    Instant uploadedAt;
}
