package com.math.taskmanager.controller;

import com.math.taskmanager.entity.Notification;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.service.NotificationService;
import com.math.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    /*
     * Lista as notificações do usuário
     */
    @GetMapping
    public ResponseEntity<List<Notification>> findMyNotifications(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.findByUser(userId)
        );
    }

    /*
     * Conta notificações não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.countUnread(userId)
        );
    }

    /*
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.noContent().build();
    }

    /*
     * Oculta uma notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id) {

        notificationService.deactivate(id);

        return ResponseEntity.noContent().build();
    }
}