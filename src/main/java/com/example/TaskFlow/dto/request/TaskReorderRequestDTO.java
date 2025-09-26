package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TaskReorderRequestDTO {
    @NotNull
    private Long projectId;

    @NotNull
    private TaskStatus status;

    @NotEmpty
    @Valid
    private List<PositionDTO> positions;

    @Data
    public static class PositionDTO {
        @NotNull
        private Long taskId;

        @NotNull
        @Min(0)
        private Integer sortOrder;
    }
}
