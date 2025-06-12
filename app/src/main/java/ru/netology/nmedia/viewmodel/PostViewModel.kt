package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0, content = "", author = "", likedByMe = false, likes = 0, published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

//    fun loadPosts() {
//        thread {
//            // Начинаем загрузку
//            _data.postValue(FeedModel(loading = true))
//            try {
//                // Данные успешно получены
//                val posts = repository.getAll()
//                FeedModel(posts = posts, empty = posts.isEmpty())
//            } catch (e: IOException) {
//                // Получена ошибка
//                FeedModel(error = true)
//            }.also(_data::postValue)
//        }
//    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallBack {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

//    fun save() {
//        edited.value?.let {
//            thread {
//                repository.save(it)
//                _postCreated.postValue(Unit)
//            }
//        }
//        edited.value = empty
//    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.CallBackById {
                override fun onSuccess(post: Post) {
                    edited.value?.let {
                        _postCreated.postValue(Unit)
                    }
                    edited.value = empty
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

//    fun likeById(id: Long) {
//        val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return
//        thread {
//            if (likedByMe) repository.dislikeById(id)
//            else repository.likeById(id)
//            _data.postValue(
//                FeedModel(
//                posts = _data.value?.posts.orEmpty().map { post ->
//                    if (post.id == id) {
//                        post.copy(
//                            likes = if (post.likedByMe) post.likes - 1 else post.likes + 1,
//                            likedByMe = !post.likedByMe
//                        )
//                    } else {
//                        post
//                    }
//                }))
//        }
//    }

    fun likeById(id: Long) {
        val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return
        if (likedByMe) repository.dislikeByIdAsync(id, object : PostRepository.CallBackById {
            override fun onSuccess(post: Post) {

                _data.postValue(
                    FeedModel(
                        posts = _data.value?.posts.orEmpty().map { post ->
                            if (post.id == id) {
                                post.copy(
                                    likes = post.likes - 1, likedByMe = !post.likedByMe
                                )
                            } else {
                                post
                            }
                        })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        })
        else repository.likeByIdAsync(id, object : PostRepository.CallBackById {
            override fun onSuccess(post: Post) {
                _data.postValue(
                    FeedModel(
                        posts = _data.value?.posts.orEmpty().map { post ->
                            if (post.id == id) {
                                post.copy(
                                    likes = post.likes + 1, likedByMe = !post.likedByMe
                                )
                            } else {
                                post
                            }
                        })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

//    fun removeById(id: Long) {
//        thread {
//            // Оптимистичная модель
//            val old = _data.value?.posts.orEmpty()
//            _data.postValue(
//                _data.value?.copy(
//                    posts = _data.value?.posts.orEmpty().filter { it.id != id })
//            )
//            try {
//                repository.removeById(id)
//            } catch (e: IOException) {
//                _data.postValue(_data.value?.copy(posts = old))
//            }
//        }
//    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()

        repository.removeByIdAsync(id, object : PostRepository.CallBackById {
            override fun onSuccess(post: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().filter { it.id != id })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }
}