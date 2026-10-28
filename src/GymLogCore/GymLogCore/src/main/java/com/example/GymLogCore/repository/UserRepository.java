package com.example.GymLogCore.repository;

import com.example.GymLogCore.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
    Db methods for user entity
*/
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
