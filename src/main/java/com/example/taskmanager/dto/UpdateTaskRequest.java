package com.example.taskmanager.dto;

import com.example.taskmanager.entities.Task;

public class UpdateTaskRequest {
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description;
}
