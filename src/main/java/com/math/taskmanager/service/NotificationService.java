package com.math.taskmanager.service;

import com.math.taskmanager.entity.Notification;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /*
     * Cria uma nova notificação
     */
    @Transactional
    public Notification create(
            User user,
            String type,
            String title,
            String message,
            String referenceType,
            Long referenceId) {

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .active(true)
                .build();

        return notificationRepository.save(notification);
    }

    /*
     * Busca as notificações visíveis do usuário
     */
    @Transactional(readOnly = true)
    public List<Notification> findByUser(Long userId) {
        return notificationRepository
                .findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
    }

    /*
     * Conta as notificações não lidas e visíveis
     */
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository
                .countByUserIdAndReadFalseAndActiveTrue(userId);
    }

    /* ADICIONANDO NOTIFICAÇÃO 
     */
     private Notification findNotificationForUser(
        Long notificationId,
        Long userId) {

    Notification notification = notificationRepository
            .findById(notificationId)
            .orElseThrow(() ->
                    new RuntimeException("Notificação não encontrada."));

    if (!notification.getUser().getId().equals(userId)) {
        throw new RuntimeException(
                "Você não tem permissão para acessar esta notificação."
        );
    }

    return notification;
}
     
     
     
    /*
     * Marca uma notificação como lida
     */
     @Transactional
     public void markAsRead(
             Long notificationId,
             Long userId) {

         Notification notification =
                 findNotificationForUser(notificationId, userId);

         notification.setRead(true);

         notificationRepository.save(notification);
     }

    /*
     * Oculta uma notificação
     */
     @Transactional
     public void deactivate(
             Long notificationId,
             Long userId) {

         Notification notification =
                 findNotificationForUser(notificationId, userId);

         notification.setActive(false);

         notificationRepository.save(notification);
     }
}