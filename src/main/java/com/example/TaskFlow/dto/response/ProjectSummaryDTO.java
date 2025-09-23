package com.example.TaskFlow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummaryDTO {

    private Long id;
    private String name;
    private Integer boardOrder;
    private Long teamId;
}
