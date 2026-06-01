package com.example.GymLogCore.controller;

import com.example.GymLogCore.domain.User;
import com.example.GymLogCore.dto.CreateWorkoutRequest;
import com.example.GymLogCore.dto.UpdateWorkoutRequest;
import com.example.GymLogCore.dto.WorkoutResponse;
import com.example.GymLogCore.services.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout")
@RequiredArgsConstructor
public class WorkoutController {
    private final WorkoutService workoutService;

    // Endpoint for getting all workouts
    // url: host...8080/api/workout (get)
    @GetMapping("/")
    public ResponseEntity<List<WorkoutResponse>> getAllWorkouts(@AuthenticationPrincipal User user) {
        Long userId = user.getId();

        List<WorkoutResponse> responses = workoutService.getAllWorkouts(userId);

        return ResponseEntity.ok(responses);
    }

    // Endpoint for getting workout by id
    // url: host...8080/api/workout (get)
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable("id") Long workoutId,
                                                          @AuthenticationPrincipal User user) {
        Long userId = user.getId();

        WorkoutResponse responses = workoutService.getWorkout(userId, workoutId);

        return ResponseEntity.ok(responses);
    }

    // Endpoint for creating workout
    // url: host...8080/api/workout (post)
    @PostMapping("/")
    public ResponseEntity<Long> createWorkout(@RequestBody CreateWorkoutRequest request,
                                              @AuthenticationPrincipal User user) {
        Long userId = user.getId();

        Long response = workoutService.createWorkout(request, userId);

        return ResponseEntity.ok(response);
    }

    // Endpoint for updating workout
    // url: host...8080/api/workout (put)
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateWorkout(@PathVariable("id") Long workoutId,
                                              @RequestBody UpdateWorkoutRequest request,
                                              @AuthenticationPrincipal User user) {
        Long userId = user.getId();

        Long response = workoutService.updateWorkout(workoutId, request, userId);

        return ResponseEntity.ok(response);
    }

    // Endpoint for updating workout
    // url: host...8080/api/workout (put)
    @PutMapping("/{id}")
    public void deleteWorkout(@PathVariable("id") Long workoutId,
                                              @AuthenticationPrincipal User user) {
        Long userId = user.getId();
        workoutService.deleteWorkout(workoutId, userId);
    }
}
