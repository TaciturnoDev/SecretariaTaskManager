package com.math.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.math.taskmanager.dto.TaskSlaDTO;
import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskPriority;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.math.taskmanager.repository.TaskRepository;

class TaskSlaServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);

    private final TaskHistoryService taskHistoryService =
            mock(TaskHistoryService.class);

    private final TaskSlaService service =
            new TaskSlaService(
                    taskRepository,
                    taskHistoryService
            );

    @Test
    void shouldKeepLowPriorityRegardlessOfDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.LOW);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(30)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(0, result.daysWithoutMovement());
        assertEquals(TaskPriority.LOW, result.currentPriority());
        assertEquals(TaskPriority.LOW, result.suggestedPriority());
    }

    @Test
    void shouldKeepPriorityWhenLastMovementAtIsNull() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(null);

        TaskSlaDTO result = service.calculate(task);

        assertEquals(0, result.daysWithoutMovement());
        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.MEDIUM, result.suggestedPriority());
    }

    @Test
    void shouldKeepMediumPriorityUntilTenDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(10)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.MEDIUM, result.suggestedPriority());
    }

    @Test
    void shouldEscalateMediumToMediumAtElevenDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(11)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.MEDIUM, result.suggestedPriority());
    }

    @Test
    void shouldEscalateMediumToHighAtTwentyOneDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(21)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.HIGH, result.suggestedPriority());
    }

    @Test
    void shouldEscalateMediumToUrgentAtTwentyEightDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(28)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.URGENT, result.suggestedPriority());
    }

    @Test
    void shouldEscalateHighToUrgentAtTwentyEightDays() {

        Task task = new Task();

        task.setPriority(TaskPriority.HIGH);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(28)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.HIGH, result.currentPriority());
        assertEquals(TaskPriority.URGENT, result.suggestedPriority());
    }

    @Test
    void shouldKeepUrgentPriority() {

        Task task = new Task();

        task.setPriority(TaskPriority.URGENT);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(40)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.URGENT, result.currentPriority());
        assertEquals(TaskPriority.URGENT, result.suggestedPriority());
    }

    @Test
    void shouldNeverDecreasePriority() {

        Task task = new Task();

        task.setPriority(TaskPriority.HIGH);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(11)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(TaskPriority.HIGH, result.currentPriority());
        assertEquals(TaskPriority.HIGH, result.suggestedPriority());
    }

    @Test
    void shouldCalculateDaysWithoutMovement() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(15)
        );

        TaskSlaDTO result = service.calculate(task);

        assertEquals(15, result.daysWithoutMovement());
        assertEquals(TaskPriority.MEDIUM, result.currentPriority());
        assertEquals(TaskPriority.MEDIUM, result.suggestedPriority());
    }
    
    @Test
    void shouldPersistMediumToHighEscalation() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(21)
        );

        TaskPriority result = service.escalateIfNecessary(task);

        assertEquals(TaskPriority.HIGH, result);
        assertEquals(TaskPriority.HIGH, task.getPriority());

        verify(taskRepository).save(task);

        verify(taskHistoryService).registerPriorityChange(
                task,
                TaskPriority.MEDIUM,
                TaskPriority.HIGH
        );
    }
    
    @Test
    void shouldPersistHighToUrgentEscalation() {

        Task task = new Task();

        task.setPriority(TaskPriority.HIGH);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(28)
        );

        TaskPriority result = service.escalateIfNecessary(task);

        assertEquals(TaskPriority.URGENT, result);
        assertEquals(TaskPriority.URGENT, task.getPriority());

        verify(taskRepository).save(task);

        verify(taskHistoryService).registerPriorityChange(
                task,
                TaskPriority.HIGH,
                TaskPriority.URGENT
        );
    }
    
    @Test
    void shouldNotPersistWhenThereIsNoEscalation() {

        Task task = new Task();

        task.setPriority(TaskPriority.MEDIUM);
        task.setLastMovementAt(
                LocalDateTime.now().minusDays(15)
        );

        TaskPriority result = service.escalateIfNecessary(task);

        assertEquals(TaskPriority.MEDIUM, result);
        assertEquals(TaskPriority.MEDIUM, task.getPriority());

        verify(taskRepository, never()).save(task);

        verify(
                taskHistoryService,
                never()
        ).registerPriorityChange(
                task,
                TaskPriority.MEDIUM,
                TaskPriority.MEDIUM
        );
    }
}