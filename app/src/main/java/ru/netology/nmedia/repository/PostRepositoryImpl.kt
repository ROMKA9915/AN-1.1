package ru.netology.nmedia.repository

import androidx.room.Entity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.fromDtoToEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import kotlin.collections.map


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it.map { it.toDto() } }

    override suspend fun fetchAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromDtoToEntity())
        } catch (e: Exception) {
            throw NetworkError
        }
    }

    override suspend fun getAll(): List<Post> {
        return try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: Exception) {
            throw NetworkError
        }
    }

    override suspend fun getAllAsync(): List<Post> {
        return dao.getAllAsync().toDto()
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

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = PostsApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            emit(body.size)
            delay(10_000)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.IO)

    override suspend fun save(post: Post) {
        try {
            PostsApi.service.save(post)
        } catch (e: Exception) {
            throw Exception("Save operation failed", e)
        }
    }
}
