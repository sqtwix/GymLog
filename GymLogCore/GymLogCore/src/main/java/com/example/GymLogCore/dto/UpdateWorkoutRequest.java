package com.example.GymLogCore.dto;

import java.time.LocalDateTime;

public record UpdateWorkoutRequest(String type,
                                   String description,
                                   LocalDateTime date,
                                   int durationInMinutes) {
}
