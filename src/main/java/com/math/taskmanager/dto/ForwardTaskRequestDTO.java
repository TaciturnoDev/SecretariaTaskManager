package com.math.taskmanager.dto;

import jakarta.validation.constraints.NotNull;

public record ForwardTaskRequestDTO(

        @NotNull(message = "O usuário de destino é obrigatório.")
        Long targetUserId,

        String comment

) {
}