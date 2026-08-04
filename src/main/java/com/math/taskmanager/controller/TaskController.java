package com.math.taskmanager.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.math.taskmanager.dto.ForwardTaskRequestDTO;
import com.math.taskmanager.dto.TaskRequestDTO;
import com.math.taskmanager.dto.TaskResponseDTO;
import com.math.taskmanager.entity.TaskStatus;
import com.math.taskmanager.dto.DelegationUserDTO;
import com.math.taskmanager.service.TaskService;
import com.math.taskmanager.service.UserService;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.service.TaskPdfService;
import com.math.taskmanager.entity.Task;

import org.springframework.security.core.Authentication;

import java.util.List;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskPdfService taskPdfService;

    /* ===================================================== */
    /*  CRIAR TAREFA                                         */
    /* ===================================================== */

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(
            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication
    ) {

        String login = authentication.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, login));
    }


    /* ===================================================== */
    /*  ATUALIZAR TAREFA                                     */
    /* ===================================================== */

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication
    ) {

        String login = authentication.getName();

        return ResponseEntity.ok(
                taskService.update(id, dto, login)
        );
    }


    /* ===================================================== */
    /*  LISTAR TAREFAS                                       */
    /* ===================================================== */

    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication
    ) {

        String login = authentication.getName();

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                taskService.findAll(userId, status, pageable, login)
        );
    }


    /* ===================================================== */
    /* USUÁRIOS DISPONÍVEIS PARA DELEGAÇÃO                   */
    /* ===================================================== */

    @GetMapping("/delegation-users")
    public ResponseEntity<List<DelegationUserDTO>> getDelegationUsers(
            Authentication authentication
    ) {

        String login = authentication.getName();

        User currentUser = userService.findByLogin(login);

        return ResponseEntity.ok(
                userService.findUsersForDelegation(currentUser)
        );
    }

    /* ===================================================== */
    /* BUSCAR TAREFA POR ID                                  */
    /* ===================================================== */

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String login = authentication.getName();

        return ResponseEntity.ok(
                taskService.findById(
                        id,
                        login
                )
        );
    }


    /* ===================================================== */
    /*  DELEGAR TAREFA                                       */
    /* ===================================================== */

    @PutMapping("/{id}/forward")
    public ResponseEntity<TaskResponseDTO> forwardTask(
            @PathVariable Long id,
            @Valid @RequestBody ForwardTaskRequestDTO dto,
            Authentication authentication
    ) {

        String login = authentication.getName();

        return ResponseEntity.ok(
                taskService.forwardTask(
                        id,
                        dto.targetUserId(),
                        dto.comment(),
                        login
                )
        );
    }


    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable Long id
    ) {

    	Task task = taskService.findEntityById(id);

    	byte[] pdf = taskPdfService.generate(task);
    	
    	
        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=RD-" + id + ".pdf"
                )
                .header(
                        "Content-Type",
                        "application/pdf"
                )
                .body(pdf);
    }
    
    /* ===================================================== */
    /*  DELETAR TAREFA                                       */
    /* ===================================================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String login = authentication.getName();

        taskService.delete(id, login);

        return ResponseEntity.noContent().build();
    }
}