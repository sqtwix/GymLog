package com.example.GymLogCore.dto;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Optional;

public record RegisterRequest(
        String username,
        String email,
        String password,
        LocalDateTime date
) {}
