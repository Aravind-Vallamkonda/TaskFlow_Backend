package com.example.TaskFlow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class TeamRequestDTO {

    private String name;
    private String description;
    private Set<Long> memberIds;
}
