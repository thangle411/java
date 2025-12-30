package com.example.taskmanager.dto;

import com.example.taskmanager.entities.Task;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateTaskRequest {
    private String description;
    @Min(0)
    @Max(100)
    private int progress;

    public String getDescription() {
        return description;
    }

    public int getProgress() {
        return progress;
    }
}
