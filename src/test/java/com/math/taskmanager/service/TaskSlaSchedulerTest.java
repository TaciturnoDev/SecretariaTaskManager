package com.math.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.math.taskmanager.dto.TaskSlaDTO;
import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskPriority;
import com.math.taskmanager.entity.TaskStatus;
import com.math.taskmanager.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskSlaSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSlaService taskSlaService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @InjectMocks
    private TaskSlaScheduler scheduler;

    @Test
    void shouldEscalateMediumToHigh() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.IN_PROGRESS);

        LocalDateTime originalLastMovement =
                LocalDateTime.now().minusDays(21);

        task.setLastMovementAt(originalLastMovement);

        when(taskRepository.findByStatusIn(anyList()))
                .thenReturn(List.of(task));

        when(taskSlaService.calculate(task))
                .thenReturn(new TaskSlaDTO(
                        21,
                        TaskPriority.MEDIUM,
                        TaskPriority.HIGH
                ));

        scheduler.applySla();

        assertEquals(
                TaskPriority.HIGH,
                task.getPriority()
        );

        /*
         * A escalação automática não altera
         * lastMovementAt.
         */
        assertEquals(
                originalLastMovement,
                task.getLastMovementAt()
        );

        verify(taskRepository)
                .save(task);

        verify(taskHistoryService)
                .register(
                        eq(task),
                        isNull(),
                        eq("SLA: aumentou prioridade de MEDIUM para HIGH"),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull()
                );
    }

    @Test
    void shouldEscalateMediumToUrgentAtTwentyEightDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);

        LocalDateTime originalLastMovement =
                LocalDateTime.now().minusDays(28);

        task.setLastMovementAt(originalLastMovement);

        when(taskRepository.findByStatusIn(anyList()))
                .thenReturn(List.of(task));

        when(taskSlaService.calculate(task))
                .thenReturn(new TaskSlaDTO(
                        28,
                        TaskPriority.MEDIUM,
                        TaskPriority.URGENT
                ));

        scheduler.applySla();

        assertEquals(
                TaskPriority.URGENT,
                task.getPriority()
        );

        assertEquals(
                originalLastMovement,
                task.getLastMovementAt()
        );

        verify(taskRepository)
                .save(task);

        verify(taskHistoryService)
                .register(
                        eq(task),
                        isNull(),
                        eq("SLA: aumentou prioridade de MEDIUM para URGENT"),
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull()
                );
    }

    @Test
    void shouldNotChangeLowPriority() {

        Task task = new Task();

        task.setPriority(TaskPriority.LOW);
        task.setStatus(TaskStatus.IN_PROGRESS);

        task.setLastMovementAt(
                LocalDateTime.now().minusDays(40)
        );

        when(taskRepository.findByStatusIn(anyList()))
                .thenReturn(List.of(task));

        when(taskSlaService.calculate(task))
                .thenReturn(new TaskSlaDTO(
                        0,
                        TaskPriority.LOW,
                        TaskPriority.LOW
                ));

        scheduler.applySla();

        assertEquals(
                TaskPriority.LOW,
                task.getPriority()
        );

        verify(taskRepository, never())
                .save(any(Task.class));

        verify(taskHistoryService, never())
                .register(
                        any(),
                        any(),
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldNeverDecreasePriority() {

        Task task = new Task();

        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.IN_PROGRESS);

        task.setLastMovementAt(
                LocalDateTime.now().minusDays(11)
        );

        when(taskRepository.findByStatusIn(anyList()))
                .thenReturn(List.of(task));

        when(taskSlaService.calculate(task))
                .thenReturn(new TaskSlaDTO(
                        11,
                        TaskPriority.HIGH,
                        TaskPriority.HIGH
                ));

        scheduler.applySla();

        assertEquals(
                TaskPriority.HIGH,
                task.getPriority()
        );

        verify(taskRepository, never())
                .save(any(Task.class));

        verify(taskHistoryService, never())
                .register(
                        any(),
                        any(),
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldNotChangeCompletedTasks() {

        when(taskRepository.findByStatusIn(anyList()))
                .thenReturn(List.of());

        scheduler.applySla();

        verify(taskRepository)
                .findByStatusIn(
                        eq(List.of(
                                TaskStatus.PENDING,
                                TaskStatus.IN_PROGRESS
                        ))
                );

        verify(taskRepository, never())
                .save(any(Task.class));
    }
}