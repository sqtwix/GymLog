package com.example.gymlogapp.repository

import com.example.gymlogapp.model.CreateTrainRequest
import com.example.gymlogapp.model.TrainResponse
import com.example.gymlogapp.utils.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class WorkoutRepository(
    private val client: HttpClient,
    private val tokenStore: TokenStore
) {
    suspend fun getWorkouts(): List<TrainResponse> = client.get("api/workout") {
        authorize()
    }.body()

    suspend fun createWorkout(request: CreateTrainRequest) {
        client.post("api/workout") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    private fun HttpRequestBuilder.authorize() {
        tokenStore.getToken()?.takeIf { it.isNotBlank() }?.let { bearerAuth(it) }
    }
}
