package ru.netology.nmedia.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl: PostRepository {

    override fun getAll(): List<Post> {
        return ApiService.service.getAll()
            .execute()
            .let { it.body() ?: throw RuntimeException("body is null") }
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallBack) {
        ApiService.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    when {
                        // Успешный ответ (200-299) с данными
                        response.isSuccessful && response.body() != null -> {
                            callback.onSuccess(response.body()!!)
                        }

                        // Успешный ответ, но тело пустое (может быть валидным сценарием)
                        response.isSuccessful -> {
                            callback.onSuccess(emptyList())
                        }

                        // Неуспешный ответ (404, 500 и др.)
                        else -> {
                            callback.onError(
                                HttpException(response).apply {
                                    Log.w("API", "HTTP ${response.code()}: ${response.errorBody()?.string()}")
                                }
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<Post>>,
                    throwable: Throwable
                ) {
                    Log.e("API", "Network error", throwable)
                    callback.onError(throwable)
                }
            })
    }

    override fun likeById(id: Long) {
//        val request: Request = Request.Builder()
//            .post(gson.toJson(id).toRequestBody(jsonType))
//            .url("${BASE_URL}/api/posts/$id/likes")
//            .build()
//
//        client.newCall(request)
//            .execute()
//            .close()
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.CallBackById<Post>) {
//        val request: Request
//        if (post.likedByMe) {
//            request = Request.Builder()
//                .delete()
//                .url("$BASE_URL/api/posts/${post.id}/likes")
//                .build()
//        } else {
//            request = Request.Builder()
//                .post("".toRequestBody())
//                .url("$BASE_URL/api/posts/${post.id}/likes")
//                .build()
//        }
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onResponse(call: Call, response: Response) {
//                if (!response.isSuccessful) {
//                    callback.onError(IOException("Ошибка обновления поста: ${response.message}"))
//                    return
//                }
//
//                val responseBody = response.body?.string() ?: run {
//                    callback.onError(IOException("budy is null"))
//                    return
//                }
//
//                val postResponse = gson.fromJson(responseBody, Post::class.java)
//                callback.onSuccess(postResponse)
//            }
//
//            override fun onFailure(call: Call, e: IOException) {
//                callback.onError(e)
//            }
//        })
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun save(post: Post) {
        ApiService.service.save(post)
            .execute()
    }

    override fun saveAsync(post: Post, callback: PostRepository.CallBackById<Post>) {
//        val request: Request = Request.Builder()
//            .post(gson.toJson(post).toRequestBody(jsonType))
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//        client.newCall(request)
//            .enqueue(object: Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        val body = response.body?.string()
//                        callback.onSuccess(gson.fromJson(body, Post::class.java))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//            })
    }

    override fun removeById(id: Long) {
        ApiService.service.deleteById(id)
            .execute()
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.CallBackById<Unit>) {
//        val request: Request = Request.Builder()
//            .delete()
//            .url("${BASE_URL}/api/slow/posts/$id")
//            .build()
//
//        client.newCall(request)
//            .enqueue(object: Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        val body = response.body?.string()
//                        callback.onSuccess(Unit)
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//            })
    }
}