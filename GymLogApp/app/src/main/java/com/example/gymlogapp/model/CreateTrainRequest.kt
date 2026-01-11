package com.example.gymlogapp.model

import java.util.Date;

data class CreateTrainRequest(
    val type: String,
    val description: String,
    val date: String,
    val duration: Int,
    val userId: Int? = null
)