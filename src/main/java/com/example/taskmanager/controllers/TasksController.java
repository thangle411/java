package com.example.taskmanager.controllers;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.entities.Task;
import com.example.taskmanager.repositories.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<Task> getTaskWithId(@PathVariable String id) {
        return taskRepository.getTaskWithId(id);
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> addNewTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        Task createdTask = taskRepository.addNewTask(request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        boolean response = taskRepository.deleteById(id);
        if(!response) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Object> updateTask(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        Optional<Task> updated = taskRepository.updateTaskById(id, request);
        return updated.<ResponseEntity<Object>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
