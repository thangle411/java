package com.example.taskmanager.dto;

import com.example.taskmanager.entities.TaskStatus;

public class UpdateTaskStatusRequest {
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }
}
