package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val published: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0,
    val views: Long = 0,
    val video: String? = null,
    val attachment: Attachment? = null,
    val isShown: Boolean = false,
)

data class Attachment(
    val url: String,
    val description: String,
    val type: String
)
