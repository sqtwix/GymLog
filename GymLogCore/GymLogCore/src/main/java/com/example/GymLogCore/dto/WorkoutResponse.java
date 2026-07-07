package com.example.GymLogCore.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponse(
        Long id,
        String type,
        String description,
        String location,
        LocalDateTime date,
        int durationInMinutes,
        List<String> bodyParts
) {
}
