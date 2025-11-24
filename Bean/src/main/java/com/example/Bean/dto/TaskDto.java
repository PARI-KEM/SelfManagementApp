package com.example.Bean.dto;

import com.example.Bean.enums.Priority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDto {

    private String title;
    private String description;
    private String dueDate; // HTML datetime-local returns string
    private Priority priority;
}
