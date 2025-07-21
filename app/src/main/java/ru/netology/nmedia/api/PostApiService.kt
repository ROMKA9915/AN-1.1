package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    })
    .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl("http://10.0.2.2:9999/api/slow/")
    .build()

interface PostsApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long)
}

object PostsApi {
    val service: PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java)
    }
}