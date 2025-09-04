package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.fromDtoToEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import kotlin.collections.map


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it.map { it.toDto() } }

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromDtoToEntity())
            dao.setShownAll()
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

    override suspend fun markAllShown() {
        dao.setShownAll()
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = PostsApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromDtoToEntity())
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

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostsApi.service.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }
}
