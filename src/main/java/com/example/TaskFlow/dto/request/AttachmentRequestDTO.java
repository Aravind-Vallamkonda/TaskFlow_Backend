package com.example.TaskFlow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttachmentRequestDTO {

    private Long taskId;
    private Long uploadedById;
    private String fileName;
    private String fileType;
    private String storageUrl;
    private Long fileSizeBytes;
}
