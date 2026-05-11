package com.example.GymLogCore.dto;

import java.text.DateFormat;
import java.util.Optional;

public record RegisterRequest(
        String username,
        String email,
        String password,
        Optional<DateFormat> date
) {}
