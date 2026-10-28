package com.example.gymlogapp.model

data class TrainsDTO(
    val type: String,
    val description: String? = null,
    val date: String,          // "yyyy-MM-dd" или "yyyy-MM-ddTHH:mm:ss"
    val duration: Int,
    val userId: Int = 0        // сервер берёт из токена, так что можно 0
)