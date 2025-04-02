package ru.netology.nmedia.dto

import ru.netology.nmedia.R

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0,
    val views: Long = 0,
    val avatar: Int = R.drawable.ic_avatar_512
)
