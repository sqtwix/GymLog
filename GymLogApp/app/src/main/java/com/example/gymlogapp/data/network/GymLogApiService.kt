package com.example.gymlogapp.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*
import kotlinx.serialization.json.Json

interface GymLogApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/workout")
    suspend fun getAllWorkouts(): List<WorkoutResponse>

    @GET("api/workout/{id}")
    suspend fun getWorkoutById(@Path("id") id: Long): WorkoutResponse

    @POST("api/workout")
    suspend fun createWorkout(@Body request: CreateWorkoutRequest): Long

    @PUT("api/workout/{id}")
    suspend fun updateWorkout(@Path("id") id: Long, @Body request: UpdateWorkoutRequest): Long

    @DELETE("api/workout/{id}")
    suspend fun deleteWorkout(@Path("id") id: Long): retrofit2.Response<Unit>

    @GET("api/body-parts")
    suspend fun getAllBodyParts(): List<BodyPart>

    @GET("api/types")
    suspend fun getAllWorkoutTypes(): List<WorkoutType>

    @GET("api/locations")
    suspend fun getAllLocations(): List<WorkoutLocation>

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(authManager: AuthManager): GymLogApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()
                    authManager.getToken()?.let { token ->
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    chain.proceed(requestBuilder.build())
                }
                .build()

            val baseUrl = authManager.getApiUrl()
            val contentType = "application/json".toMediaType()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(GymLogApiService::class.java)
        }
    }
}
