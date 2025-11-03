package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

sealed class PostsState {
    object Loading : PostsState()
    data class Success(val posts: List<Post>) : PostsState()
    data class Error(val exception: Exception) : PostsState()
}