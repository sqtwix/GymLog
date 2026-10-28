package com.example.GymLogCore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.GymLogCore.domain.*;

public interface TypeRepository extends JpaRepository<Type, Long> {
}
