package com.capstone.belink.Network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    private var instance: Retrofit?=null
    private val gson = GsonBuilder().setLenient().create()

    private const val BASE_URL = "http://10.0.2.2:3000"

    //SingleTon
    fun getInstance(context: Context): Retrofit{
        if(instance == null){
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                    .client(okhttpClient(context))//인터셉터를 추가하기 위한 구문
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        return instance!!
    }

    private fun okhttpClient(context: Context):OkHttpClient{
        return OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))//okhttpclient 에 인터셉터를 추가한다.
                .build()
    }
}

