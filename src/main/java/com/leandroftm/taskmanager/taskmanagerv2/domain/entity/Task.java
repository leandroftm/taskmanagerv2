package com.leandroftm.taskmanager.taskmanagerv2.domain.entity;

import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskPriority;
import com.leandroftm.taskmanager.taskmanagerv2.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@NoArgsConstructor
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false, length = 50)
    private String title;
    @Column(name = "description", length = 255)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false, length = 25)
    private TaskStatus taskStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false, length = 25)
    private TaskPriority taskPriority;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "due_date",  nullable = false)
    private LocalDateTime dueDate;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(TaskStatus taskStatus, TaskPriority taskPriority, LocalDateTime dueDate) {
        this.taskStatus = taskStatus;
        this.taskPriority = taskPriority;
        this.dueDate = dueDate;
    }

}
