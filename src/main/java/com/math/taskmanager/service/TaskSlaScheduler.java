package com.math.taskmanager.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskStatus;
import com.math.taskmanager.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSlaScheduler {

    private final TaskRepository taskRepository;
    private final TaskSlaService taskSlaService;

    /*
     * Executa automaticamente a cada hora.
     *
     * O SLA é baseado em dias, mas a execução horária evita
     * depender de um único horário do dia.
     */
    @Scheduled(cron = "${task.sla.cron:0 0 * * * *}")
    @Transactional
    public void applySla() {

        /*
         * Somente tarefas não concluídas participam
         * da verificação automática de SLA.
         */
        List<Task> tasks = taskRepository.findByStatusIn(
                List.of(
                        TaskStatus.PENDING,
                        TaskStatus.IN_PROGRESS
                )
        );

        for (Task task : tasks) {

            /*
             * Toda a lógica de escalada fica centralizada
             * no TaskSlaService.
             */
            taskSlaService.escalateIfNecessary(task);
        }
    }
}