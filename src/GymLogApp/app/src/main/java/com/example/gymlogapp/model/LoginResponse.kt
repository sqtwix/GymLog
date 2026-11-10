package com.example.gymlogapp.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse? = null
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String
)
