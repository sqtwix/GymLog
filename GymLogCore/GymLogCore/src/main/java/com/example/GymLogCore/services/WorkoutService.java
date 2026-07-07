package com.example.GymLogCore.services;

import com.example.GymLogCore.domain.*;
import com.example.GymLogCore.dto.CreateWorkoutRequest;
import com.example.GymLogCore.dto.UpdateWorkoutRequest;
import com.example.GymLogCore.dto.WorkoutResponse;
import com.example.GymLogCore.repository.LocationRepository;
import com.example.GymLogCore.repository.TypeRepository;
import com.example.GymLogCore.repository.UserRepository;
import com.example.GymLogCore.repository.WorkoutRepository;
import com.example.GymLogCore.repository.BodyPartRepository;
import org.springframework.transaction.annotation.Transactional;
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
    private final LocationRepository locationRepository;
    private final TypeRepository typeRepository;
    private final BodyPartRepository bodyPartRepository;


    @Transactional(readOnly = true)
    public List<WorkoutResponse> getAllWorkouts(long userId) {
        return workoutRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkout(long userId, long workoutId) {
        Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        return mapToResponse(workout);
    }

    @Transactional
    public Long createWorkout(CreateWorkoutRequest request, long userId){
        Workout workout = new Workout();
        Type type = typeRepository.findById(request.type_id())
                .orElseThrow(() -> new RuntimeException("Type not found"));
        workout.setType(type);

        Location location = null;
        if (request.location_id() != null) {
            location = locationRepository.findById(request.location_id())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
        }
        workout.setLocation(location);

        workout.setDescription(request.description());
        workout.setDate(request.date());
        workout.setDurationMinutes(request.durationInMinutes());

        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found"));
        workout.setUser(user);

        if (request.bodyPartIds() != null && !request.bodyPartIds().isEmpty()) {
            List<BodyPart> bodyParts = bodyPartRepository.findAllById(request.bodyPartIds());
            workout.setBodyParts(bodyParts);
        }

        workoutRepository.save(workout);
        return workout.getId();
    }

    @Transactional
    public Long updateWorkout(long workoutId, UpdateWorkoutRequest request, long userId) {
        Workout existingWorkout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        Type type = typeRepository.findById(request.type_id())
                .orElseThrow(() -> new RuntimeException("Type not found"));
        existingWorkout.setType(type);

        Location location = null;
        if (request.location_id() != null) {
            location = locationRepository.findById(request.location_id())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
        }
        existingWorkout.setLocation(location);

        existingWorkout.setDescription(request.description());
        existingWorkout.setDate(request.date());
        existingWorkout.setDurationMinutes(request.durationInMinutes());

        if (request.bodyPartIds() != null && !request.bodyPartIds().isEmpty()) {
            List<BodyPart> bodyParts = bodyPartRepository.findAllById(request.bodyPartIds());
            existingWorkout.setBodyParts(bodyParts);
        } else {
            existingWorkout.setBodyParts(new ArrayList<>());
        }

        workoutRepository.save(existingWorkout);
        return existingWorkout.getId();
    }

    @Transactional
    public void deleteWorkout(long workoutId, long userId){
        Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        workoutRepository.delete(workout);
    }

    private WorkoutResponse mapToResponse(Workout w) {
        String locationName = (w.getLocation() != null) ? w.getLocation().getLocation() : null;
        List<String> bodyPartNames = w.getBodyParts() != null ? 
                w.getBodyParts().stream().map(BodyPart::getPartName).toList() : new ArrayList<>();

        return new WorkoutResponse(
                w.getId(),
                w.getType().getType(),
                w.getDescription(),
                locationName,
                w.getDate(),
                w.getDurationMinutes(),
                bodyPartNames
        );
    }
}

