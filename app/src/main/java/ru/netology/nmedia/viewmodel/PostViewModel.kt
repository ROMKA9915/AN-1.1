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

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.CallBackById<Post> {
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

    fun likeById(id: Long) {
        val post = _data.value?.posts?.find { it.id == id } ?: return
        val updatedPost = post.copy(
            likedByMe = !post.likedByMe,
            likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
        )
        _data.value?.posts?.map { if (it.id == id) updatedPost else it }?.let {
            _data.postValue(_data.value?.copy(posts = it))
        }

        repository.likeByIdAsync(post, object : PostRepository.CallBackById<Post> {
            override fun onSuccess(result: Post) {
                _data.value?.posts?.let { posts ->
                    val updatedPosts = posts.map {
                        if (it.id == id) {
                            result.copy(published = it.published)
                        } else it
                    }
                    _data.postValue(_data.value?.copy(posts = updatedPosts))
                }
            }

            override fun onError(e: Exception) {
                _data.value?.posts?.let { posts ->
                    val updatedPosts = posts.map {
                        if (it.id == id) post else it
                    }
                    _data.postValue(_data.value?.copy(posts = updatedPosts))
                }
            }
        })
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(
                posts = _data.value?.posts.orEmpty().filter { it.id != id })
        )

        repository.removeByIdAsync(id, object : PostRepository.CallBackById<Unit> {
            override fun onSuccess(result: Unit) {
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }
}