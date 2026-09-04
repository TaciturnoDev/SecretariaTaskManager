package com.math.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.math.taskmanager.dto.TaskRequestDTO;
import com.math.taskmanager.dto.TaskResponseDTO;
import com.math.taskmanager.entity.*;
import com.math.taskmanager.exception.BusinessRuleException;
import com.math.taskmanager.repository.TaskRepository;
import com.math.taskmanager.repository.TaskHistoryRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private SectorService sectorService;
    
    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private TaskSlaService taskSlaService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskSuccessfully() {

        String login = "admin";

        // ===== SECTOR =====
        Sector sector = new Sector();
        sector.setId(1L);
        sector.setName("TI");

        // ===== USER =====
        User user = new User();
        user.setId(1L);
        user.setName("Marcos");
        user.setLogin(login);
        user.setSector(sector);
        user.setRole(Role.USER);

        // ===== DTO =====
        TaskRequestDTO dto = new TaskRequestDTO(
                "Teste",
                "Descrição teste",
                TaskStatus.PENDING,
                TaskPriority.MEDIUM,
                1L
        );

        // ===== TASK =====
        Task task = Task.builder()
                .id(1L)
                .title("Teste")
                .description("Descrição teste")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .createdBy(user)
                .assignedTo(user)
                .sector(sector)
                .build();

        task.setCreatedAt(LocalDateTime.now());

        when(userService.findByLogin(login)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponseDTO response = taskService.create(dto, login);

        assertNotNull(response);

        assertEquals("Teste", response.title());
        assertEquals(TaskStatus.PENDING, response.status());
        assertEquals(TaskPriority.MEDIUM, response.priority());

        assertEquals(1L, response.assignedToId());
        assertEquals("Marcos", response.assignedToName());

        assertEquals(1L, response.createdById());
        assertEquals("Marcos", response.createdByName());
    }

    @Test
    void shouldThrowErrorWhenUserHasNoSector() {

        String login = "admin";

        User user = new User();
        user.setLogin(login);
        user.setRole(Role.USER);

        TaskRequestDTO dto = new TaskRequestDTO(
                "Teste",
                "Descrição",
                null,
                null,
                null
        );

        when(userService.findByLogin(login)).thenReturn(user);

        assertThrows(
                BusinessRuleException.class,
                () -> taskService.create(dto, login)
        );
    }
}