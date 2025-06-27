package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getAll(): List<Post>
    fun getAllAsync(callback: GetAllCallBack)
    fun likeById(id: Long)
    fun likeByIdAsync(post: Post, callback: CallBackById<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun removeByIdAsync(id: Long, callback: CallBackById<Unit>)
    fun save(post:Post)
    fun saveAsync (post:Post, callback: CallBackById<Post>)

    interface GetAllCallBack {
        fun onSuccess (post: List<Post>)
        fun onError (e: Throwable)
    }

    interface CallBackById<T> {
        fun onSuccess (result: T)
        fun onError (e: Throwable)
    }
}