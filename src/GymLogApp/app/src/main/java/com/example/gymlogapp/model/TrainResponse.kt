package com.example.gymlogapp.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainResponse(
    val type: String,
    val description: String? = null,
    val location: String? = null,
    val date: String,          // "yyyy-MM-dd" или "yyyy-MM-ddTHH:mm:ss"
    val durationInMinutes: Int
)
