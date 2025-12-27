package com.example.taskmanager.controllers;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.entities.Task;
import com.example.taskmanager.repositories.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TasksController {
    private final TaskRepository taskRepository;

    public TasksController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/tasks")
    public Map<String, Task> getTasks() {
        return taskRepository.getTasks();
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Object> getTaskWithId(@PathVariable String id) {
        return taskRepository.getTaskWithId(id);
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> addNewTask(
            @RequestBody CreateTaskRequest request
    ) {
        Task createdTask = taskRepository.addNewTask(request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }
}
