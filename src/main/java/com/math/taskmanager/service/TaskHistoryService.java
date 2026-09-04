package com.math.taskmanager.service;

import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskHistory;
import com.math.taskmanager.entity.TaskPriority;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.repository.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskHistoryService {

    private final TaskHistoryRepository repository;

    /* ================= REGISTRAR HISTÓRICO ================= */

    public TaskHistory register(
            Task task,
            User user,
            String action,
            String oldTitle,
            String newTitle,
            String oldDescription,
            String newDescription
    ) {

        TaskHistory history = TaskHistory.builder()

                .task(task)

                .user(user)

                .action(action)

                .oldTitle(oldTitle)

                .newTitle(newTitle)

                .oldDescription(oldDescription)

                .newDescription(newDescription)

                .createdAt(LocalDateTime.now())

                .build();

        return repository.save(history);
    }

    
    /* ================= REGISTRAR DELEGAÇÃO ================= */

    public TaskHistory registerDelegation(
            Task task,
            User fromUser,
            User delegatedTo,
            String comment
    ) {

        TaskHistory history = TaskHistory.builder()

                .task(task)

                .user(fromUser)

                .delegatedTo(delegatedTo)

                .comment(comment)

                .action("Delegou a tarefa")

                .createdAt(LocalDateTime.now())

                .build();

        return repository.save(history);
    }
    
    
    /* ================= LISTAR HISTÓRICO ================= */

    public List<TaskHistory> findByTask(Long taskId) {

        return repository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
    
    
    //================= METODO CONTADOR DE DIAS ============//
    
    public TaskHistory registerPriorityChange(
            Task task,
            TaskPriority oldPriority,
            TaskPriority newPriority
    ) {
        TaskHistory history = TaskHistory.builder()
                .task(task)
                .action("Escalonamento automático por SLA")
                .oldPriority(oldPriority)
                .newPriority(newPriority)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(history);
    }
    
}