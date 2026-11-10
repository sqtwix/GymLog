package com.example.gymlogapp.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE_URL = "http://127.0.0.1:8081/"

    fun create(
        engine: HttpClientEngine = Android.create(),
        baseUrl: String = BASE_URL
    ): HttpClient = HttpClient(engine) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
        defaultRequest {
            url(baseUrl)
            accept(ContentType.Application.Json)
        }
    }
}
