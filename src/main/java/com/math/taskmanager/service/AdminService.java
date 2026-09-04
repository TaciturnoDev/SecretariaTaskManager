package com.math.taskmanager.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.math.taskmanager.entity.*;
import com.math.taskmanager.exception.BusinessRuleException;
import com.math.taskmanager.exception.ResourceNotFoundException;
import com.math.taskmanager.repository.TaskRepository;
import com.math.taskmanager.repository.UserRepository;

import com.math.taskmanager.dto.AdminUserDTO;
import com.math.taskmanager.dto.TaskResponseDTO;

import java.util.List;
import com.math.taskmanager.dto.TaskHistoryResponseDTO;

import com.math.taskmanager.dto.AttachmentResponseDTO;
import com.math.taskmanager.dto.TaskSlaDTO;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SectorService sectorService;
    private final TaskSlaService taskSlaService;

    /* ===================================================== */
    /*  LISTAR TODOS USUÁRIOS (COM DTO)                    */
    /* ===================================================== */
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(AdminUserDTO::from);
    }

    /* ===================================================== */
    /*  LISTAR TODAS TAREFAS (COM DTO)                     */
    /* ===================================================== */
    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /* ===================================================== */
    /*  ALTERAR ROLE                                       */
    /* ===================================================== */
    public void changeUserRole(Long userId, Role newRole) {

        if (newRole == null) {
            throw new BusinessRuleException("Role inválida.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado")
                );

        //  não permitir remover último SUPERADMIN
        if (user.getRole() == Role.SUPERADMIN && newRole != Role.SUPERADMIN) {

            long totalSuperAdmins = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRole() == Role.SUPERADMIN)
                    .count();

            if (totalSuperAdmins <= 1) {
                throw new BusinessRuleException("Não é possível remover o último SUPERADMIN.");
            }
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    /* ===================================================== */
    /*  ALTERAR SETOR                                      */
    /* ===================================================== */
    public void changeUserSector(Long userId, Long sectorId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado")
                );

        Sector sector = sectorService.findById(sectorId);

        user.setSector(sector);
        userRepository.save(user);
    }

    /* ===================================================== */
    /*  DESATIVAR USUÁRIO                                  */
    /* ===================================================== */
    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado")
                );

        user.setActive(false);
        userRepository.save(user);
    }

    /* ===================================================== */
    /*  DELETE REAL (USAR COM CUIDADO)                     */
    /* ===================================================== */
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado")
                );

        //  proteção básica
        if (user.getRole() == Role.SUPERADMIN) {
            throw new BusinessRuleException("Não é permitido deletar SUPERADMIN.");
        }

        userRepository.delete(user);
    }

    
    
    /* ===================================================== */
    /*  MAPPER TASK → DTO                                  */
    /* ===================================================== */
    private TaskResponseDTO mapToResponse(Task task) {

        TaskSlaDTO sla = taskSlaService.calculate(task);

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
                : List.of(),

                sla

        );

}
    }

