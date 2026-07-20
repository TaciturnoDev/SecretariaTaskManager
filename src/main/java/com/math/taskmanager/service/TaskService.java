package com.math.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.math.taskmanager.dto.TaskRequestDTO;
import com.math.taskmanager.dto.TaskResponseDTO;
import com.math.taskmanager.entity.*;
import com.math.taskmanager.exception.BusinessRuleException;
import com.math.taskmanager.exception.ResourceNotFoundException;
import com.math.taskmanager.repository.TaskRepository;
import com.math.taskmanager.repository.TaskHistoryRepository;

import java.util.List;
import com.math.taskmanager.dto.TaskHistoryResponseDTO;
import java.time.LocalDateTime;

import com.math.taskmanager.dto.AttachmentResponseDTO;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final SectorService sectorService;
    private final TaskHistoryService taskHistoryService;
    private final TaskHistoryRepository taskHistoryRepository;

    /* ===================================================== */
    /* CRIAR TAREFA                                          */
    /* ===================================================== */

    public TaskResponseDTO create(TaskRequestDTO dto, String login) {

        User user = userService.findByLogin(login);

        Sector sector;

        if (user.getRole() == Role.SUPERADMIN) {

            if (dto.sectorId() == null) {
                throw new BusinessRuleException("SUPERADMIN deve informar o setor.");
            }

            sector = sectorService.findById(dto.sectorId());

        } else {

            if (user.getSector() == null) {
                throw new BusinessRuleException("Usuário não está vinculado a um setor.");
            }

            sector = user.getSector();
        }

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(
                        dto.status() != null
                                ? dto.status()
                                : TaskStatus.PENDING
                )
                .priority(
                        dto.priority() != null
                                ? dto.priority()
                                : TaskPriority.MEDIUM
                )
                .createdBy(user)
                .assignedTo(user)
                .sector(sector)
                .lastMovementAt(LocalDateTime.now())
                .build();

        task = taskRepository.save(task);

        /* ================= HISTÓRICO ================= */

        taskHistoryService.register(
                task,
                user,
                "Criou a tarefa",

                null,
                task.getTitle(),

                null,
                task.getDescription()
        );

        return mapToResponse(task);
    }

    /* ===================================================== */
    /* LISTAR TAREFAS                                        */
    /* ===================================================== */

    public Page<TaskResponseDTO> findAll(
            Long userId,
            TaskStatus status,
            Pageable pageable,
            String login
    ) {

        User user = userService.findByLogin(login);

        Page<Task> page;

        // SUPERADMIN vê tudo
        if (user.getRole() == Role.SUPERADMIN) {

            if (userId != null && status != null) {

                page = taskRepository.findByAssignedToIdAndStatus(
                        userId,
                        status,
                        pageable
                );

            } else if (userId != null) {

                page = taskRepository.findByAssignedToId(
                        userId,
                        pageable
                );

            } else if (status != null) {

                page = taskRepository.findByStatus(
                        status,
                        pageable
                );

            } else {

                page = taskRepository.findAll(pageable);
            }

        } else {

            if (user.getSector() == null) {
                throw new BusinessRuleException("Usuário sem setor.");
            }

            page = taskRepository.findBySectorId(
                    user.getSector().getId(),
                    pageable
            );
        }

        return page.map(this::mapToResponse);
    }
    
    
    /* REGISTRO HISTORICO */
    
public void touchTask(Task task) {
	task.setLastMovementAt(
			LocalDateTime.now()
			);
	
	taskRepository.save(task);
	
}
    
    
    
    /* ===================================================== */
    /* BUSCAR TAREFA POR ID                                  */
    /* ===================================================== */

    public TaskResponseDTO findById(
            Long id,
            String login
    ) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tarefa não encontrada"
                        )
                );

        User user = userService.findByLogin(login);

        // SUPERADMIN pode visualizar tudo
        if (user.getRole() == Role.SUPERADMIN) {
            return mapToResponse(task);
        }

        if (user.getSector() == null) {
            throw new BusinessRuleException(
                    "Usuário sem setor."
            );
        }

        if (!task.getSector().getId().equals(
                user.getSector().getId()
        )) {

            throw new BusinessRuleException(
                    "Você não pode visualizar tarefas de outro setor."
            );
        }

        return mapToResponse(task);
    }

    /* ===================================================== */
    /* ATUALIZAR TAREFA                                      */
    /* ===================================================== */

    public TaskResponseDTO update(Long id, TaskRequestDTO dto, String login) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada")
                );

        User user = userService.findByLogin(login);

        /* ================= SNAPSHOT ANTIGO ================= */

        String oldStatus = task.getStatus().name();

        String oldPriority = task.getPriority().name();

        String oldTitle = task.getTitle();

        String oldDescription = task.getDescription();

        // =====================================================
        // SUPERADMIN
        // =====================================================

        if (user.getRole() == Role.SUPERADMIN) {

            task.setTitle(dto.title());

            task.setDescription(dto.description());

            if (dto.status() != null) {
                task.setStatus(dto.status());
            }

            if (dto.priority() != null) {
                task.setPriority(dto.priority());
            }
            
            // ADIÇÃO PARA URGENCIA EM DIAS
            touch(task); 
            
           
            Task updatedTask = taskRepository.save(task);

            registerUpdateHistory(
                    updatedTask,
                    user,
                    oldStatus,
                    oldPriority,
                    oldTitle,
                    oldDescription
            );

            return mapToResponse(updatedTask);
        }

        // =====================================================
        // USUÁRIO COMUM
        // =====================================================

        if (user.getSector() == null) {
            throw new BusinessRuleException("Usuário sem setor.");
        }

        if (!task.getSector().getId().equals(user.getSector().getId())) {

            throw new BusinessRuleException(
                    "Você não pode editar tarefas de outro setor."
            );
        }

        task.setTitle(dto.title());

        task.setDescription(dto.description());

        if (dto.status() != null) {
            task.setStatus(dto.status());
        }

        if (dto.priority() != null) {
            task.setPriority(dto.priority());
        }

        /* segundo save */
        touch(task);
        
        Task updatedTask = taskRepository.save(task);

        registerUpdateHistory(
                updatedTask,
                user,
                oldStatus,
                oldPriority,
                oldTitle,
                oldDescription
        );

        return mapToResponse(updatedTask);
    }

    /* ==================== DELEGAR TAREFA ===================*/

    public TaskResponseDTO forwardTask(
            Long taskId,
            Long targetUserId,
            String comment,
            String login
    ) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada")
                );

        User currentUser = userService.findByLogin(login);

        User targetUser = userService.findById(targetUserId);

        // =====================================================
        // VALIDAÇÕES
        // =====================================================

        // Não permitir delegar para o mesmo responsável
        if (task.getAssignedTo().getId().equals(targetUser.getId())) {
            throw new BusinessRuleException(
                    "A tarefa já está atribuída a este usuário."
            );
        }

        // SUPERADMIN pode delegar qualquer tarefa
        if (currentUser.getRole() != Role.SUPERADMIN) {

            // Usuário deve possuir setor
            if (currentUser.getSector() == null) {
                throw new BusinessRuleException("Usuário sem setor.");
            }

            // Só pode delegar tarefas do próprio setor
            if (!task.getSector().getId().equals(currentUser.getSector().getId())) {
                throw new BusinessRuleException(
                        "Você não pode delegar tarefas de outro setor."
                );
            }

            // USER só pode delegar tarefas atribuídas a ele
            if (currentUser.getRole() == Role.USER &&
                    !task.getAssignedTo().getId().equals(currentUser.getId())) {

                throw new BusinessRuleException(
                        "Você só pode delegar tarefas atribuídas a você."
                );
            }

            // Destinatário deve pertencer ao mesmo setor
            if (targetUser.getSector() == null ||
                    !targetUser.getSector().getId().equals(currentUser.getSector().getId())) {

                throw new BusinessRuleException(
                        "A tarefa só pode ser delegada para usuários do mesmo setor."
                );
            }
        }

     // =====================================================
     // DELEGAÇÃO
     // =====================================================

     User previousResponsible = task.getAssignedTo();

     task.setAssignedTo(targetUser);

     /*
      * Quando o SUPERADMIN delega para outro setor,
      * a tarefa acompanha o novo responsável.
      */
     if (targetUser.getSector() != null) {
         task.setSector(targetUser.getSector());
     }

     touch(task);

     Task updatedTask = taskRepository.save(task);

        // =====================================================
        // HISTÓRICO
        // =====================================================

        String historyComment =
                (comment == null || comment.isBlank())
                        ? "Delegou a tarefa de "
                        + previousResponsible.getName()
                        + " para "
                        + targetUser.getName()
                        : comment;

        taskHistoryService.registerDelegation(
                updatedTask,
                currentUser,
                targetUser,
                historyComment
        );

        return mapToResponse(updatedTask);
    }
    
    /* ===================================================== */
    /* DELETAR TAREFA                                        */
    /* ===================================================== */

    public void delete(Long id, String login) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada")
                );

        User user = userService.findByLogin(login);

        // SUPERADMIN pode tudo
        if (user.getRole() == Role.SUPERADMIN) {

            taskHistoryService.register(
                    task,
                    user,
                    "Deletou a tarefa",

                    task.getTitle(),
                    null,

                    task.getDescription(),
                    null
            );

            taskRepository.delete(task);

            return;
        }

        if (user.getSector() == null) {
            throw new BusinessRuleException("Usuário sem setor.");
        }

        if (!task.getSector().getId().equals(user.getSector().getId())) {

            throw new BusinessRuleException(
                    "Você não pode deletar tarefas de outro setor."
            );
        }

        taskHistoryService.register(
                task,
                user,
                "Deletou a tarefa",

                task.getTitle(),
                null,

                task.getDescription(),
                null
        );

        taskRepository.delete(task);
    }

    /* ===================================================== */
    /* HISTÓRICO UPDATE                                      */
    /* ===================================================== */

    private void registerUpdateHistory(
            Task task,
            User user,
            String oldStatus,
            String oldPriority,
            String oldTitle,
            String oldDescription
    ) {

        /* ================= STATUS ================= */

        if (!oldStatus.equals(task.getStatus().name())) {

            taskHistoryService.register(
                    task,
                    user,
                    "Alterou status de " + oldStatus + " para " + task.getStatus().name(),
                    null,
                    null,
                    null,
                    null
            );
        }

        /* ================= PRIORIDADE ================= */

        if (!oldPriority.equals(task.getPriority().name())) {

            taskHistoryService.register(
                    task,
                    user,
                    "Alterou prioridade de " + oldPriority + " para " + task.getPriority().name(),
                    null,
                    null,
                    null,
                    null
            );
        }

        /* ================= ALTERAÇÃO DE CONTEÚDO ================= */

        boolean titleChanged =
                oldTitle != null && !oldTitle.equals(task.getTitle());

        boolean descriptionChanged =
                oldDescription != null && !oldDescription.equals(task.getDescription());

        if (titleChanged || descriptionChanged) {

            taskHistoryService.register(
                    task,
                    user,
                    "Atualizou a tarefa",
                    oldTitle,
                    task.getTitle(),
                    oldDescription,
                    task.getDescription()
            );
        }
    }
    
    /* 	ADIÇÃO PARA URGENCIA EM DIAS */
	
	private void touch(Task task) {
	    task.setLastMovementAt(LocalDateTime.now());
	}


    /* ===================================================== */
    /* MAPPER                                                */
    /* ===================================================== */

    private TaskResponseDTO mapToResponse(Task task) {

        return new TaskResponseDTO(

                task.getId(),

                task.getTitle(),

                task.getDescription(),

                task.getStatus(),

                task.getPriority(),

                task.getCreatedAt(),

                task.getUpdatedAt(),

                task.getAssignedTo().getId(),

                task.getAssignedTo().getName(),

                task.getCreatedBy().getId(),

                task.getCreatedBy().getName(),

                task.getHistory() != null
                ? task.getHistory().stream()
                .map(history -> new TaskHistoryResponseDTO(

                        history.getId(),

                        history.getAction(),

                        history.getUser() != null
                                ? history.getUser().getName()
                                : "Sistema",

                        history.getOldTitle(),
                        history.getNewTitle(),

                        history.getOldDescription(),
                        history.getNewDescription(),

                        history.getCreatedAt(),

                        history.getAttachments() != null
                                ? history.getAttachments().stream()

                                .filter(att ->
                                        Boolean.TRUE.equals(
                                                att.getActive()
                                        )
                                )

                                .map(att -> new AttachmentResponseDTO(

                                        att.getId(),

                                        att.getOriginalFileName(),

                                        att.getFileSize(),

                                        att.getContentType()

                                ))

                                .toList()

                                : List.of()

                ))
                .toList()
                : List.of()
        );        
    }
}