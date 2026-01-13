package com.example.gymlogapp.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.gymlogapp.utils.Preferences

object RetrofitClient {

    private const val BASE_URL = "http://192.168.0.11:5198/"

    // Сохраняем Application Context здесь (инициализируется один раз)
    private var appContext: Context? = null

    // Вызывается один раз в начале приложения
    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext // важно: applicationContext!
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                // Теперь берём токен из сохранённого контекста
                val token = appContext?.let { Preferences.getToken(it) } ?: ""
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                if (token.isNotEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}