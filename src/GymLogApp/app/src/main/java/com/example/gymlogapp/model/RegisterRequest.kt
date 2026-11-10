package com.example.gymlogapp.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val gender: String,
    val birthDate: String
)
