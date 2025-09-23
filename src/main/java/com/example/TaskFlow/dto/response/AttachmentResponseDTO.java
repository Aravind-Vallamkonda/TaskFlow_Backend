package com.example.TaskFlow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDTO {

    private Long id;
    private Long taskId;
    private Long uploadedById;
    private String fileName;
    private String fileType;
    private String storageUrl;
    private Long fileSizeBytes;
    private Instant createdAt;
    private Instant updatedAt;
}
