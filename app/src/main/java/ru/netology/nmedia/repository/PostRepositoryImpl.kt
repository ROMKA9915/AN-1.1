package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 25),
        pagingSourceFactory = dao::getPagingSource,
        remoteMediator = PostRemoteMediator(apiService = apiService, postDao = dao)
    ).flow.map { it.map(PostEntity::toDto) }

//    override suspend fun getAll() {
//        try {
//            val response = apiService.getAll()
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            dao.insert(body.fromDtoToEntity())
//            dao.setShownAll()
//        } catch (e: Exception) {
//            throw NetworkError
//        }
//    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            apiService.deleteById(id)
        } catch (e: Exception) {
            throw Exception("Remove operation failed", e)
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            apiService.likeById(id)
        } catch (e: Exception) {
            throw Exception("Like operation failed", e)
        }
    }

    override suspend fun markAllShown() {
        dao.setShownAll()
    }

//    override fun getNewer(id: Long): Flow<Int> = flow {
//        while (true) {
//            val response = apiService.getNewer(id)
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            dao.insert(body.fromDtoToEntity())
//            emit(body.size)
//            delay(10_000)
//        }
//    }.catch { e -> throw AppError.from(e) }.flowOn(Dispatchers.IO)

    override suspend fun save(post: Post) {
        try {
            val postFinal = post.copy(
                authorId = AppAuth.getInstance().authStateFlow.value.id,
            )
            apiService.save(postFinal)
        } catch (e: Exception) {
            throw Exception("Save operation failed", e)
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(
                attachment = Attachment(media.id, AttachmentType.IMAGE),
                authorId = AppAuth.getInstance().authStateFlow.value.id,
            )
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

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun signInUser(login: String, pass: String) {
        val updatedUser = apiService.updateUser(login, pass).body()
        if (updatedUser != null) {
            AppAuth.getInstance().setAuth(updatedUser.id, updatedUser.token)
        } else {
            throw Exception("Incorrect login or password")
        }
    }
}
