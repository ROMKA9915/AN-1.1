package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0,
    val views: Long = 0,
    val video: String? = null,
    val isShown: Boolean = false,
    val ownedByMe: Boolean = false,
    @Embedded
    var attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        authorId = authorId,
        authorAvatar = authorAvatar,
        published = published,
        content = content,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        views = views,
        video = video,
        isShown = isShown,
        ownedByMe = ownedByMe,
        attachment = attachment?.toDto(),
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            dto.id,
            dto.author,
            dto.authorId,
            dto.authorAvatar,
            dto.published,
            dto.content,
            dto.likedByMe,
            dto.likes,
            dto.shares,
            dto.views,
            dto.video,
            dto.isShown,
            dto.ownedByMe,
            AttachmentEmbeddable.fromDto(dto.attachment),
        )
    }

    data class AttachmentEmbeddable(
        var url: String,
        var type: AttachmentType,
    ) {
        fun toDto() = Attachment(url, type)

        companion object {
            fun fromDto(dto: Attachment?) = dto?.let {
                AttachmentEmbeddable(it.url, it.type)
            }
        }
    }
}

fun List<PostEntity>.toDto() = map(PostEntity::toDto)
fun List<Post>.fromDtoToEntity() = map { PostEntity.fromDto(it) }