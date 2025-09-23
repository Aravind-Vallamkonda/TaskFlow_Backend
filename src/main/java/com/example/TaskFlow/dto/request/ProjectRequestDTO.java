package com.example.TaskFlow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectRequestDTO {

    private Long teamId;
    private String name;
    private String description;
    private Integer boardOrder;
}
