package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.data.ApiException
import ru.netology.nmedia.data.AuthRequiredException
import ru.netology.nmedia.data.NetworkException
import ru.netology.nmedia.data.NotFoundException
import ru.netology.nmedia.data.ServerErrorException
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl: PostRepository {

    override fun getAll(): List<Post> {
        return ApiService.service.getAll()
            .execute()
            .let { it.body() ?: throw RuntimeException("body is null") }
    }

    override fun save(post: Post) {
        ApiService.service.save(post)
            .execute()
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }


    override fun removeById(id: Long) {
        ApiService.service.deleteById(id)
            .execute()
    }

    override fun likeById(id: Long) {
        ApiService.service.likeById(id)
            .execute()
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallBack) {
        ApiService.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    when {
                        response.isSuccessful -> {
                            val body = response.body() ?: emptyList()
                            callback.onSuccess(body)
                        }
                        response.code() == 401 -> callback.onError(AuthRequiredException())
                        response.code() == 404 -> callback.onError(NotFoundException())
                        response.code() in 500..599 -> callback.onError(ServerErrorException())
                        else -> callback.onError(ApiException("Ошибка ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(NetworkException())
                }
            })
    }

    override fun saveAsync(post: Post, callback: PostRepository.CallBackById<Post>) {
        ApiService.service.save(post)
            .enqueue(object: Callback<Post> {
                override fun onResponse(
                    call: Call<Post?>,
                    response: Response<Post?>
                ) {
                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("body is null"))
                        return
                    }
                    callback.onSuccess(body)
                }

                override fun onFailure(
                    call: Call<Post?>,
                    t: Throwable
                ) {
                    callback.onError(t)
                }
            })
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.CallBackById<Post>) {
        if (post.likedByMe) {
            ApiService.service.dislikeById(post.id)
                .enqueue(object: Callback<Post>{
                    override fun onResponse(
                        call: Call<Post?>,
                        response: Response<Post?>
                    ) {
                        val body = response.body() ?: run {
                            callback.onError(RuntimeException("body is null"))
                            return
                        }
                        callback.onSuccess(body)
                    }

                    override fun onFailure(
                        call: Call<Post?>,
                        t: Throwable
                    ) {
                        callback.onError(t)
                    }
                })
        } else {
            ApiService.service.likeById(post.id).enqueue(object: Callback<Post>{
                override fun onResponse(
                    call: Call<Post?>,
                    response: Response<Post?>
                ) {
                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("body is null"))
                        return
                    }
                    callback.onSuccess(body)
                }

                override fun onFailure(
                    call: Call<Post?>,
                    t: Throwable
                ) {
                    callback.onError(t)
                }
            })
        }
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.CallBackById<Unit>) {
        ApiService.service.deleteById(id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit?>,
                    response: Response<Unit?>
                ) {
                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("body is null"))
                        return
                    }
                    callback.onSuccess(body)
                }

                override fun onFailure(call: Call<Unit?>, t: Throwable) {
                    callback.onError(t)                }
            })
    }
}