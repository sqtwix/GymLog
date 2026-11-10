package com.example.gymlogapp.utils

interface TokenStore {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
