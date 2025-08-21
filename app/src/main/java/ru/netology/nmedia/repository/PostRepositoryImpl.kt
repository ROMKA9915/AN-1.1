package ru.netology.nmedia.repository

import androidx.room.Entity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.fromDtoToEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError


class PostRepositoryImpl(private val dao: PostDao): PostRepository {

    override val data = dao.getAll().map { it.map { it.toDto() } }

    override suspend fun getAll() {
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

    override suspend fun getNewerCount(): Int {
        return try {
            val latestPostId = dao.getLatestPostId() ?: 0L
            val response =  PostsApi.service.getNewer(latestPostId, 1)
            response.count { it.id > latestPostId }
        } catch (e: Exception) {
            0
        }    }

    override suspend fun save(post: Post) {
        try {
            PostsApi.service.save(post)
        } catch (e: Exception) {
            throw Exception("Save operation failed", e)
        }    }

//    override fun getNewer(id: Long): Flow<Int> = flow {
//        while (true) {
//            delay(10_000L)
//            val response = PostsApi.service.getNewer(id)
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            dao.insert(body.fromDtoToEntity())
//            emit(body.size)
//        }
//    }.catch { e-> throw AppError.from(e) }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                // Проверяем количество новых постов
                val count = getNewPostsCount(id)
                if (count > 0) {
                    emit(count)
                }
            } catch (e: Exception) {
                // Логируем ошибку, но продолжаем работу
                delay(30_000) // Задержка при ошибке
            }
            delay(30_000) // Проверка каждые 30 секунд
        }
    }.catch { e ->
        // Обрабатываем ошибки потока
        emit(0)
    }

    private suspend fun getNewPostsCount(sinceId: Long): Int {
        return try {
            val response = PostsApi.service.getNewer(sinceId, 1)
            response.count { it.id > sinceId }
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun loadNewPosts(sinceId: Long): List<Post> {
        return try {
            val newPosts = PostsApi.service.getNewer(sinceId, 20)
                .filter { it.id > sinceId }

            if (newPosts.isNotEmpty()) {
                dao.insert(newPosts.fromDtoToEntity())
            }

            newPosts
        } catch (e: Exception) {
            emptyList()
        }
    }
}
