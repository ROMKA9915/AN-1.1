package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getAll(): List<Post>
    fun getAllAsync(callback: GetAllCallBack)
    fun likeById(id: Long)
    fun likeByIdAsync(id: Long, callback: CallBackById)
    fun dislikeById(id: Long)
    fun dislikeByIdAsync(id: Long, callback: CallBackById)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun removeByIdAsync(id: Long, callback: CallBackById)
    fun save(post:Post)
    fun saveAsync (post:Post, callback: CallBackById)

    interface GetAllCallBack {
        fun onSuccess (post: List<Post>)
        fun onError (e: Exception)
    }

    interface CallBackById {
        fun onSuccess (post: Post)
        fun onError (e: Exception)
    }
}