package com.example.GymLogCore.dto;

import java.time.LocalDateTime;

public record CreateWorkoutRequest(
        Long type_id,
        Long location_id,
        String description,
        LocalDateTime date,
        int durationInMinutes
) {
}
