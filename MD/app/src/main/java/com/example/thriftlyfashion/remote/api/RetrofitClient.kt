package com.example.thriftlyfashion.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "http://192.168.180.210:3000"
//    private const val BASE_URL = "https://thriftly-backend-456846068275.us-central1.run.app/"
    private const val BASE_URL = "https://x1hnjxjj-3000.asse.devtunnels.ms/"

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return instance.create(serviceClass)
    }
}
