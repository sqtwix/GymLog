package com.example.GymLogCore.repository;

import com.example.GymLogCore.domain.User;
import com.example.GymLogCore.domain.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
   Db methods for workout entity
*/
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByUserId(Long userId);
    Optional<Workout> findByIdAndUserId(long workoutId,long userId);
}
