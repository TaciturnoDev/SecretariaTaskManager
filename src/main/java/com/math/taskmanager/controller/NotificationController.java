package com.math.taskmanager.controller;

import com.math.taskmanager.entity.Notification;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.service.NotificationService;
import com.math.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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
            Authentication authentication) {

        User user =
                userService.findByLogin(authentication.getName());

        return ResponseEntity.ok(
                notificationService.findByUser(user.getId())
        );
    }

    /*
     * Conta notificações não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(
            Authentication authentication) {

        User user =
                userService.findByLogin(authentication.getName());

        return ResponseEntity.ok(
                notificationService.countUnread(user.getId())
        );
    }

    /*
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        User user =
                userService.findByLogin(authentication.getName());

        notificationService.markAsRead(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    /*
     * Oculta uma notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id,
            Authentication authentication) {

        User user =
                userService.findByLogin(authentication.getName());

        notificationService.deactivate(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}