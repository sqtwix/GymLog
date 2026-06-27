package com.example.GymLogCore.dto;

import java.time.LocalDateTime;

public record WorkoutResponse(
        String type,
        String description,
        String location,
        LocalDateTime date,
        int durationInMinutes
) {
}
