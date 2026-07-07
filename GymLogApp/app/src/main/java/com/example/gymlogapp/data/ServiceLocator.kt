package com.example.gymlogapp.data

import android.content.Context
import com.example.gymlogapp.data.network.AuthManager
import com.example.gymlogapp.data.network.GymLogApiService

object ServiceLocator {
    private var authManagerInstance: AuthManager? = null
    private var apiServiceInstance: GymLogApiService? = null

    fun getAuthManager(context: Context): AuthManager {
        return authManagerInstance ?: synchronized(this) {
            val instance = AuthManager(context.applicationContext)
            authManagerInstance = instance
            instance
        }
    }

    fun getApiService(context: Context): GymLogApiService {
        val auth = getAuthManager(context)
        return apiServiceInstance ?: synchronized(this) {
            val instance = GymLogApiService.create(auth)
            apiServiceInstance = instance
            instance
        }
    }

    fun resetApiService() {
        synchronized(this) {
            apiServiceInstance = null
        }
    }
}
