package com.leandroftm.taskmanager.taskmanagerv2.controller;

import com.leandroftm.taskmanager.taskmanagerv2.domain.entity.Task;
import com.leandroftm.taskmanager.taskmanagerv2.dto.CreateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.dto.TaskResponse;
import com.leandroftm.taskmanager.taskmanagerv2.dto.UpdateTaskRequest;
import com.leandroftm.taskmanager.taskmanagerv2.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        TaskResponse taskResponse = taskService.createTask(createTaskRequest);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(taskResponse.id())
                .toUri();

        return ResponseEntity.created(uri).body(taskResponse);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(@PageableDefault(size = 10, sort = "createdAt")
                                                          Pageable pageable) {
        Page<TaskResponse> taskResponsePage = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(taskResponsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.getById(id);
        return ResponseEntity.ok(taskResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTask(@PathVariable Long id,
                                           @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {
        taskService.updateTask(id, updateTaskRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
