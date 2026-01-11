package com.example.gymlogapp.model

data class TrainResponse(
    val id: Int,
    val type: String,
    val description: String,
    val date: String,
    val duration: Int
)