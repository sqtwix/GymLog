package com.example.gymlogapp.model

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String
)