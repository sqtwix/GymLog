package com.example.GymLogCore.repository;

import com.example.GymLogCore.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
