package com.math.taskmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.math.taskmanager.dto.TaskSlaDTO;
import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskPriority;
import com.math.taskmanager.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSlaService {

    private final TaskRepository taskRepository;
    private final TaskHistoryService taskHistoryService;
    private final NotificationService notificationService;

    public TaskSlaDTO calculate(Task task) {

        TaskPriority currentPriority = task.getPriority();

        /*
         * LOW não participa do SLA automático.
         */
        if (currentPriority == TaskPriority.LOW) {

            return new TaskSlaDTO(
                    0,
                    currentPriority,
                    currentPriority
            );
        }

        /*
         * URGENT já é a maior prioridade.
         */
        if (currentPriority == TaskPriority.URGENT) {

            return new TaskSlaDTO(
                    0,
                    currentPriority,
                    currentPriority
            );
        }

        /*
         * Para tarefas antigas, priorityChangedAt pode estar NULL.
         *
         * Nesse caso usamos lastMovementAt como início
         * do ciclo atual do SLA.
         */
        LocalDateTime cycleStart = task.getPriorityChangedAt();

        if (cycleStart == null) {
            cycleStart = task.getLastMovementAt();
        }

        if (cycleStart == null) {

            return new TaskSlaDTO(
                    0,
                    currentPriority,
                    currentPriority
            );
        }

        long daysWithoutMovement =
                calculateDaysWithoutMovement(cycleStart);

        TaskPriority suggestedPriority =
                calculateSuggestedPriority(
                        currentPriority,
                        daysWithoutMovement
                );

        return new TaskSlaDTO(
                daysWithoutMovement,
                currentPriority,
                suggestedPriority
        );
    }

    /*
     * ============================================================
     * ESCALONAMENTO AUTOMÁTICO POR SLA
     * ============================================================
     *
     * Este método é responsável por:
     *
     * 1. verificar se existe escalada;
     * 2. alterar a prioridade;
     * 3. iniciar novo ciclo de SLA;
     * 4. persistir a alteração;
     * 5. registrar histórico;
     * 6. enviar notificação.
     */
    @Transactional
    public TaskPriority escalateIfNecessary(Task task) {

        TaskSlaDTO sla = calculate(task);

        TaskPriority currentPriority = sla.currentPriority();
        TaskPriority suggestedPriority = sla.suggestedPriority();

        /*
         * Não existe necessidade de escalada.
         */
        if (getPriorityLevel(suggestedPriority)
                <= getPriorityLevel(currentPriority)) {

            return currentPriority;
        }

        TaskPriority oldPriority = currentPriority;

        /*
         * Atualiza a prioridade.
         */
        task.setPriority(suggestedPriority);

        /*
         * IMPORTANTE:
         *
         * A escalada automática NÃO é movimentação.
         *
         * Portanto:
         * - lastMovementAt NÃO muda;
         * - priorityChangedAt inicia o novo ciclo.
         */
        task.setPriorityChangedAt(LocalDateTime.now());

        /*
         * Persiste a nova prioridade.
         */
        taskRepository.save(task);

        /*
         * Registra a alteração oficial no histórico.
         */
        taskHistoryService.registerPriorityChange(
                task,
                oldPriority,
                suggestedPriority
        );

        /*
         * Notifica o responsável pela tarefa.
         */
        notificationService.create(
                task.getAssignedTo(),
                "TASK_SLA_ESCALATED",
                "Prioridade elevada por SLA",
                "A tarefa \"" + task.getTitle()
                        + "\" teve a prioridade elevada de "
                        + oldPriority
                        + " para "
                        + suggestedPriority
                        + " por falta de movimentação.",
                "TASK",
                task.getId()
        );

        return suggestedPriority;
    }

    private TaskPriority calculateSuggestedPriority(
            TaskPriority currentPriority,
            long daysWithoutMovement
    ) {

        /*
         * MEDIUM → HIGH após 15 dias.
         */
        if (currentPriority == TaskPriority.MEDIUM
                && daysWithoutMovement >= 15) {

            return TaskPriority.HIGH;
        }

        /*
         * HIGH → URGENT após 10 dias.
         */
        if (currentPriority == TaskPriority.HIGH
                && daysWithoutMovement >= 10) {

            return TaskPriority.URGENT;
        }

        return currentPriority;
    }

    private int getPriorityLevel(TaskPriority priority) {

        return switch (priority) {

            case LOW -> 1;

            case MEDIUM -> 2;

            case HIGH -> 3;

            case URGENT -> 4;
        };
    }

    private long calculateDaysWithoutMovement(
            LocalDateTime cycleStart
    ) {

        return ChronoUnit.DAYS.between(
                cycleStart.toLocalDate(),
                LocalDate.now()
        );
    }
}