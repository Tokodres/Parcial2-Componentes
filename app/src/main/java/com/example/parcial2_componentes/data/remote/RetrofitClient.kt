package com.example.parcial2_componentes.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val TIMEOUT_SECONDS = 30L // ✅ Aumentar timeout

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // ✅ Timeout de conexión
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)    // ✅ Timeout de lectura
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)   // ✅ Timeout de escritura
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES)) // ✅ Pool de conexiones
        .dispatcher(Dispatcher().apply {
            maxRequests = 20
            maxRequestsPerHost = 10
        })
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}