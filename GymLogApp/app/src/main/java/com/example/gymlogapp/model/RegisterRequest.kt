package com.example.gymlogapp.model

import java.util.Date;

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val gender: String,
    val birthDay: String
)