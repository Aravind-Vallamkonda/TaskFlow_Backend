package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttachmentPayloadDTO {
    @NotBlank
    @Size(max = 255)
    private String fileName;

    @Size(max = 100)
    private String contentType;

    @NotBlank
    @Size(max = 512)
    private String storageUrl;

    @NotNull
    private Long fileSize;
}
