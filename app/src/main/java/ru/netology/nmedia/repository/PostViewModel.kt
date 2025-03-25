package ru.netology.nmedia.repository

import androidx.lifecycle.ViewModel

class PostViewModel: ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()
    fun like() = repository.like()
    fun liked() = repository.liked()
    fun disliked() = repository.disliked()
    fun shared() = repository.shared()
}