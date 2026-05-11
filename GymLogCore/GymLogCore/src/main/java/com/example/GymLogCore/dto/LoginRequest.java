package com.example.GymLogCore.dto;

public record LoginRequest(
        String email,
        String password
) {
}
