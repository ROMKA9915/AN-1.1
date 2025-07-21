package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.fromDtoToEntity
import ru.netology.nmedia.entity.toDto


class PostRepositoryImpl(private val dao: PostDao): PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.toDto()
    }

    override suspend fun getAll() {
        val posts: List<Post> = PostsApi.service.getAll()

        dao.insert(posts.fromDtoToEntity())
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            PostsApi.service.deleteById(id)
        } catch (e: Exception) {
            throw Exception("Remove operation failed", e)
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            PostsApi.service.likeById(id)
        } catch (e: Exception) {
            throw Exception("Like operation failed", e)
        }
    }
    override suspend fun save(post: Post) {
        TODO("Not yet implemented")
    }
}
