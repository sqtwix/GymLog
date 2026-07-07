package com.example.gymlogapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val gender: String,
    val birthDate: String
)

@Serializable
data class CreateWorkoutRequest(
    val type_id: Long,
    val location_id: Long?,
    val description: String,
    val date: String,
    val durationInMinutes: Int,
    val bodyPartIds: List<Long>?
)

@Serializable
data class UpdateWorkoutRequest(
    val type_id: Long,
    val description: String,
    val location_id: Long?,
    val date: String,
    val durationInMinutes: Int,
    val bodyPartIds: List<Long>?
)

@Serializable
data class WorkoutResponse(
    val id: Long,
    val type: String,
    val description: String?,
    val location: String?,
    val date: String,
    val durationInMinutes: Int,
    val bodyParts: List<String> = emptyList()
)

@Serializable
data class WorkoutType(
    val id: Long,
    val type: String
)

@Serializable
data class WorkoutLocation(
    val id: Long,
    val location: String
)

@Serializable
data class BodyPart(
    val id: Long,
    val partName: String
)
