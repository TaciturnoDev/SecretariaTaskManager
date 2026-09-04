package com.math.taskmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndAssignedToId(Long id, Long assignedToId);

    Page<Task> findAll(Pageable pageable);

    List<Task> findByStatusIn(List<TaskStatus> statuses);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByAssignedToId(Long assignedToId, Pageable pageable);

    Page<Task> findByAssignedToIdAndStatus(
            Long assignedToId,
            TaskStatus status,
            Pageable pageable
    );

    Page<Task> findBySectorId(Long sectorId, Pageable pageable);

}