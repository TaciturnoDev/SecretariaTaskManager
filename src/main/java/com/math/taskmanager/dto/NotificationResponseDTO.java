package com.math.taskmanager.dto;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        String type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        Boolean read,
        LocalDateTime createdAt
) {
}