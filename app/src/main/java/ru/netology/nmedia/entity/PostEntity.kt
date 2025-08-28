package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0,
    val views: Long = 0,
    val video: String? = null,
    val isShown: Boolean = false,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        published = published,
        content = content,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        views = views,
        video = video,
        isShown = isShown
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.author,
            dto.authorAvatar,
            dto.published,
            dto.content,
            dto.likedByMe,
            dto.likes,
            dto.shares,
            dto.views,
            dto.video,
            dto.isShown
        )
    }
}

fun List<PostEntity>.toDto() = map(PostEntity::toDto)
fun List<Post>.fromDtoToEntity() = map { PostEntity.fromDto(it) }