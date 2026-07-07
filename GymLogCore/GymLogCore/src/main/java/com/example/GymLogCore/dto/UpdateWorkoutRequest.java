package com.example.GymLogCore.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateWorkoutRequest(Long type_id,
                                   String description,
                                   Long location_id,
                                   LocalDateTime date,
                                   int durationInMinutes,
                                   List<Long> bodyPartIds) {
}
