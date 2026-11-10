package com.example.gymlogapp

import android.app.Application
import com.example.gymlogapp.api.ApiClient
import com.example.gymlogapp.repository.AuthRepository
import com.example.gymlogapp.repository.WorkoutRepository
import com.example.gymlogapp.utils.Preferences

class GymLogApplication : Application() {
    private val tokenStore by lazy { Preferences(this) }
    private val client by lazy { ApiClient.create() }

    val authRepository by lazy { AuthRepository(client, tokenStore) }
    val workoutRepository by lazy { WorkoutRepository(client, tokenStore) }
}
