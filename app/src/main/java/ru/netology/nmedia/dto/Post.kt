package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
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
    val ownedByMe: Boolean = false,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
