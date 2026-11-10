package com.example.gymlogapp.repository

import com.example.gymlogapp.model.LoginRequest
import com.example.gymlogapp.model.LoginResponse
import com.example.gymlogapp.model.RegisterRequest
import com.example.gymlogapp.utils.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepository(
    private val client: HttpClient,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): LoginResponse {
        val response = client.post("api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body<LoginResponse>()

        check(response.token.isNotBlank())
        tokenStore.saveToken(response.token)
        return response
    }

    suspend fun register(request: RegisterRequest): LoginResponse {
        val response = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<LoginResponse>()

        check(response.token.isNotBlank())
        tokenStore.saveToken(response.token)
        return response
    }

    fun logout() {
        tokenStore.clearToken()
    }
}
