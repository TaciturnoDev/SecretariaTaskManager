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

    public TaskSlaDTO calculate(Task task) {

        TaskPriority currentPriority = task.getPriority();

        /*
         * LOW não participa da escalada automática.
         * Permanece LOW independentemente dos dias.
         */
        if (currentPriority == TaskPriority.LOW) {

            return new TaskSlaDTO(
                    0,
                    currentPriority,
                    currentPriority
            );
        }

        /*
         * Sem lastMovementAt não é possível calcular
         * os dias sem movimentação.
         */
        if (task.getLastMovementAt() == null) {

            return new TaskSlaDTO(
                    0,
                    currentPriority,
                    currentPriority
            );
        }

        long daysWithoutMovement = calculateDaysWithoutMovement(
                task.getLastMovementAt()
        );

        TaskPriority suggestedPriority = calculateSuggestedPriority(
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
     * Este método é responsável por PERSISTIR a escalada.
     *
     * O calculate() continua sendo apenas uma consulta/cálculo.
     */
    @Transactional
    public TaskPriority escalateIfNecessary(Task task) {

        TaskSlaDTO sla = calculate(task);

        TaskPriority currentPriority = task.getPriority();
        TaskPriority suggestedPriority = sla.suggestedPriority();

        /*
         * Não existe necessidade de escalada.
         */
        if (getPriorityLevel(suggestedPriority)
                <= getPriorityLevel(currentPriority)) {

            return currentPriority;
        }

        /*
         * Guarda a prioridade anterior
         * antes de alterar a tarefa.
         */
        TaskPriority oldPriority = currentPriority;

        /*
         * Atualiza a prioridade da tarefa.
         */
        task.setPriority(suggestedPriority);

        /*
         * Persiste a nova prioridade.
         */
        taskRepository.save(task);

        /*
         * Registra a alteração no histórico.
         */
        taskHistoryService.registerPriorityChange(
                task,
                oldPriority,
                suggestedPriority
        );

        return suggestedPriority;
    }

    private TaskPriority calculateSuggestedPriority(
            TaskPriority currentPriority,
            long daysWithoutMovement
    ) {

        /*
         * 28+ dias → URGENT
         */
        if (daysWithoutMovement >= 28) {

            return higherPriority(
                    currentPriority,
                    TaskPriority.URGENT
            );
        }

        /*
         * 21–27 dias → HIGH
         */
        if (daysWithoutMovement >= 21) {

            return higherPriority(
                    currentPriority,
                    TaskPriority.HIGH
            );
        }

        /*
         * 11–20 dias → MEDIUM
         */
        if (daysWithoutMovement >= 11) {

            return higherPriority(
                    currentPriority,
                    TaskPriority.MEDIUM
            );
        }

        /*
         * Até 10 dias, mantém a prioridade atual.
         */
        return currentPriority;
    }

    private TaskPriority higherPriority(
            TaskPriority currentPriority,
            TaskPriority suggestedPriority
    ) {

        /*
         * A prioridade nunca diminui.
         */
        if (getPriorityLevel(currentPriority)
                >= getPriorityLevel(suggestedPriority)) {

            return currentPriority;
        }

        return suggestedPriority;
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
            LocalDateTime lastMovementAt
    ) {

        return ChronoUnit.DAYS.between(
                lastMovementAt.toLocalDate(),
                LocalDate.now()
        );
    }
}