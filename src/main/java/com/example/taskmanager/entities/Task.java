package com.example.taskmanager.entities;

import java.time.Instant;

public class Task {

    private String id;
    private String description;
    private TaskStatus status;
    private String createdAt;
    private String updatedAt;
    private int progress;

    public Task(String id, String description, TaskStatus status, String createdAt, String updatedAt, int progress) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.progress = progress;
    }

    public void setDescription(String description) {
        if(description != null) {
            this.description = description;
        }
    }

    public void setProgress(int progress) {
        if (status != TaskStatus.IN_PROGRESS ){
            throw new IllegalStateException("Progress can only be updated when task is IN_PROGRESS");
        }

        if(progress < 0 || progress > 100) {
            throw new IllegalStateException("Progress must be between 0 and 100");
        }

        this.progress = progress;
    }

    public void setUpdatedAt() {
        this.updatedAt = Instant.now().toString();
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getProgress() {
        return progress;
    }

    public void markAsDone() {
        this.progress = 100;
        this.status = TaskStatus.DONE;
    }

    public void markAsNotStarted() {
        this.progress = 0;
        this.status = TaskStatus.NOT_STARTED;
    }

    public void markAsInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void markAsCancelled() {
        this.status = TaskStatus.CANCELLED;
    }
}
