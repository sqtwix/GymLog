package com.example.GymLogCore.services;

import com.example.GymLogCore.domain.*;
import com.example.GymLogCore.dto.CreateWorkoutRequest;
import com.example.GymLogCore.dto.UpdateWorkoutRequest;
import com.example.GymLogCore.dto.WorkoutResponse;
import com.example.GymLogCore.repository.UserRepository;
import com.example.GymLogCore.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public List<WorkoutResponse> getAllWorkouts(long userId) {
        return workoutRepository.findAllByUserId(userId).stream()
                .map(w -> new WorkoutResponse(
                        w.getType(),
                        w.getDescription(),
                        w.getDate(),
                        w.getDurationMinutes()))
                .toList();
    }

    public WorkoutResponse getWorkout(long userId, long workoutId) {
        Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        return new WorkoutResponse(
                workout.getType(),
                workout.getDescription(),
                workout.getDate(),
                workout.getDurationMinutes()
        );
    }

    public Long createWorkout(CreateWorkoutRequest request, long userId){
        Workout workout = new Workout();
        workout.setType(request.type());
        workout.setDescription(request.description());
        workout.setDate(request.date());
        workout.setDurationMinutes(request.durationInMinutes());

        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found"));
        workout.setUser(user);

        workoutRepository.save(workout);
        return workout.getId();
    }

    public Long updateWorkout(long workoutId, UpdateWorkoutRequest request, long userId) {
        Workout existingWorkout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        existingWorkout.setType(request.type());
        existingWorkout.setDescription(request.description());
        existingWorkout.setDate(request.date());
        existingWorkout.setDurationMinutes(request.durationInMinutes());

        workoutRepository.save(existingWorkout);
        return existingWorkout.getId();
    }

    public void deleteWorkout(long workoutId, long userId){
        Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        workoutRepository.delete(workout);
    }
}
