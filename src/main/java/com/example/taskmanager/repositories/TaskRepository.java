package com.example.taskmanager.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.entities.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Repository
public class TaskRepository {

    private static final Logger log = (Logger) LoggerFactory.getLogger(TaskRepository.class);
    private final ObjectMapper objectMapper;
    private final Path filePath = Path.of("data.json");
    private Map<String, Task> tasksCache = new HashMap<String, Task>();

    public TaskRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private void ensureFileExists() {
        if(Files.exists(filePath)) {
            System.out.println("File already exists: " + filePath);
            return;
        }

        try {
            log.info("Creating data.json file at {}", filePath);
            Files.writeString(filePath, "{}", StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read tasks from " + filePath,
                    e
            );
        }
    }

    public Map<String, Task> getTasks() {
        this.ensureFileExists();

        try {
            String json = Files.readString(filePath);

            System.out.println("json: " + json);
            if(json.isBlank()) {
                Map<String, Task> blankList = new HashMap<>();
                tasksCache = blankList;
                return blankList;
            }

            Map<String, Task> tasks = objectMapper.readValue(json, new TypeReference<Map<String, Task>>() {});
            tasksCache = tasks;
            return tasks;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read tasks from " + filePath,
                    e
            );
        }
    }

    public ResponseEntity<Object> getTaskWithId(String id) {
        if(tasksCache.isEmpty()) {
            tasksCache.putAll(this.getTasks());
        }

        Task task = this.tasksCache.get(id);

        if(task == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(task);
    }

    public Task addNewTask(String description) {
        Map<String, Task> tasks = getTasks();
        String nextId = getNextId(tasks);
        String now = Instant.now().toString();
        Task newTask = new Task(nextId, description, "not-started", now , "");
        tasks.put(nextId, newTask);
        saveTasks(tasks);
        return newTask;
    }

    public boolean deleteById(String id) {
        Map<String, Task> tasks = getTasks();
        if(!tasks.containsKey(id)) {
            return false;
        }
        tasks.remove(id);
        saveTasks(tasks);
        return true;
    }

    public Optional<Task> updateTaskById(String id, UpdateTaskRequest task) {
        Map<String, Task> tasks = getTasks();
        Task currentTask = tasks.get(id);

        if(currentTask == null) {
            return Optional.empty();
        }
        currentTask.setDescription(task.getDescription());
        tasks.put(id, currentTask);
        saveTasks(tasks);
        return Optional.of(currentTask);
    }

    private void saveTasks(Map<String, Task> tasks) {
        try {
            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(tasks);

            Files.writeString(
                    filePath,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save tasks to " + filePath, e);
        }
    }

    private String getNextId(Map<String, Task> tasks) {
        return String.valueOf(
                tasks.keySet().stream()
                        .mapToInt(Integer::parseInt)
                        .max()
                        .orElse(0) + 1
        );
    }
}
