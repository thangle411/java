package com.example.taskmanager.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.dto.UpdateTaskStatusRequest;
import com.example.taskmanager.entities.Task;
import com.example.taskmanager.entities.TaskStatus;
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
    private Map<String, Task> tasks;

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

    public Map<String, Task> getTasks(TaskStatus status, Integer progress) {
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

            Map<String, Task> filteredTasks = tasks.entrySet().stream()
                    .filter(e -> status == null || e.getValue().getStatus() == status)
                    .filter(e -> progress == null || e.getValue().getProgress() <= progress)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));

            return filteredTasks;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read tasks from " + filePath,
                    e
            );
        }
    }

    public Optional<Task> getTaskWithId(String id) {
        if(tasksCache.isEmpty()) {
            tasksCache.putAll(this.getTasks(null,null));
        }

        Task task = this.tasksCache.get(id);

        if(task == null) {
            return Optional.empty();
        }

        return Optional.of(task);
    }

    public Task addNewTask(String description) {
        Map<String, Task> tasks = getTasks(null, null);
        String nextId = getNextId(tasks);
        String now = Instant.now().toString();
        Task newTask = new Task(nextId, description, TaskStatus.NOT_STARTED, now , "", 0);
        tasks.put(nextId, newTask);
        saveTasks(tasks);
        return newTask;
    }

    public boolean deleteById(String id) {
        Map<String, Task> tasks = getTasks(null,null);
        if(!tasks.containsKey(id)) {
            return false;
        }
        tasks.remove(id);
        saveTasks(tasks);
        return true;
    }

    public Optional<Task> updateTaskDetailsById(String id, UpdateTaskRequest task) {
        Map<String, Task> tasks = getTasks(null, null);
        Task currentTask = tasks.get(id);

        if(currentTask == null) {
            return Optional.empty();
        }
        currentTask.setDescription(task.getDescription());
        currentTask.setProgress(task.getProgress());
        currentTask.setUpdatedAt();
        tasks.put(id, currentTask);
        saveTasks(tasks);
        return Optional.of(currentTask);
    }

    public Optional<Task> updateTaskStatusById(String id, UpdateTaskStatusRequest task) {
        Map<String, Task> tasks = getTasks(null, null);
        Task currentTask = tasks.get(id);

        if(currentTask == null) {
            return Optional.empty();
        }

        switch (task.getStatus()) {
            case TaskStatus.NOT_STARTED -> currentTask.markAsNotStarted();
            case TaskStatus.IN_PROGRESS -> currentTask.markAsInProgress();
            case TaskStatus.DONE -> currentTask.markAsDone();
            case TaskStatus.CANCELLED -> currentTask.markAsCancelled();
        }

        currentTask.setUpdatedAt();
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
