package com.example.GymLogCore.dto;

import java.time.LocalDateTime;

public record CreateWorkoutRequest(
        String type,
        String description,
        LocalDateTime date,
        int durationInMinutes
) {
}
