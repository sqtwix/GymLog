package com.example.gymlogapp.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateTrainRequest(
    val type_id: Long,
    val location_id: Long? = null,
    val description: String? = null,
    val date: String,
    val durationInMinutes: Int
)
