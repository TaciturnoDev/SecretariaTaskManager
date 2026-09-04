package com.math.taskmanager.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.math.taskmanager.entity.Task;
import com.math.taskmanager.entity.TaskPriority;
import com.math.taskmanager.entity.TaskStatus;
import com.math.taskmanager.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSlaScheduler {
	
	 private final TaskRepository taskRepository;
	 private final TaskSlaService taskSlaService;
	 private final TaskHistoryService taskHistoryService;

	 /*
	     * Executa automaticamente a cada hora.
	     *
	     * O SLA é baseado em dias, mas a execução horária evita
	     * depender de um único horário do dia.
	     */
	 
	 @Scheduled(cron = "${task.sla.cron:0 0 * * * *}")
	 @Transactional
	 public void applySla() {
		 
		 List<Task> tasks = taskRepository.findByStatusIn(
				 List.of(
						 TaskStatus.PENDING,
						 TaskStatus.IN_PROGRESS
						 
				 )
		 );
		 
		 for (Task task : tasks) {
			 
			 var sla = taskSlaService.calculate(task);
			 
			 TaskPriority currentPriority = sla.currentPriority();
			 TaskPriority suggestedPriority = sla.suggestedPriority();
			 

	            /*
	             * Só existe ação quando o SLA realmente determina
	             * uma prioridade maior.
	             */
			 
			if (suggestedPriority == currentPriority) {
					continue;
		 }
			
			task.setPriority(suggestedPriority);
			
			 /*
             * IMPORTANTE:
             * Não chamamos touch() nem touchTask().
             *
             * A escalação automática NÃO é movimentação da demanda.
             * Portanto, lastMovementAt permanece intacto.
             */
			
			taskRepository.save(task);
			
			taskHistoryService.register(
					task,
					null,
					"SLA: aumentou prioridade de "
					       + currentPriority
					       + " para "
					       + suggestedPriority,
					       
			null,
			null,
			null,
			null
			
		);
	  }
   } 
}