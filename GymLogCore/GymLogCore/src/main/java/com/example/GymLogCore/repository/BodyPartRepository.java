package com.example.GymLogCore.repository;

import com.example.GymLogCore.domain.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BodyPartRepository extends JpaRepository<BodyPart, Long> {
}
