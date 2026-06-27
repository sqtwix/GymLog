package com.example.gymlogapp.api

import retrofit2.http.*
import com.example.gymlogapp.model.*   // ← здесь будут твои модели (LoginRequest и т.д.)

interface ApiService {

    @POST("api/User/register")
    suspend fun register(@Body dto: RegisterRequest): Any  // или конкретный response тип

    @POST("api/User/login")
    suspend fun login(@Body dto: LoginRequest): LoginResponse

    @GET("api/trains")
    suspend fun getTrains(): List<TrainResponse>

    @POST("api/trains/train")
    suspend fun createTrain(@Body dto: CreateTrainRequest): TrainResponse

    @DELETE("api/trains/delete/{date}")
    suspend fun deleteTrain(@Path("date") date: String): Any  // можно Map<String, String>
}