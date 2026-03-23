package com.leandroftm.taskmanager.taskmanagerv2.repository;

import com.leandroftm.taskmanager.taskmanagerv2.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
