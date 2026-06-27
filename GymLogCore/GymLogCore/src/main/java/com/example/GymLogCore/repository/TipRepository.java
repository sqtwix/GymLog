package com.example.GymLogCore.repository;

import com.example.GymLogCore.domain.Tip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {
    @Query(value = "SELECT * FROM tips ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Tip> getRandomTip();
}
