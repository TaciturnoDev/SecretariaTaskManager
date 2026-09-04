package com.math.taskmanager.dto;

import com.math.taskmanager.entity.TaskPriority;

public record TaskSlaDTO(
        long daysWithoutMovement,
        TaskPriority currentPriority,
        TaskPriority suggestedPriority
) {}

