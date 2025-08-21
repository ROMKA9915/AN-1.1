package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.NewPostsState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0, content = "", author = "", likedByMe = false, likes = 0, published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao
    )
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> = repository.data.map { FeedModel(it, it.isEmpty()) }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
            .catch { _state.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default)
    }
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _newPostsState = MutableStateFlow<NewPostsState>(NewPostsState.Hidden)
    val newPostsState: Flow<NewPostsState> = _newPostsState
    private var latestPostId: Long = 0L
    private var newPostsJob: Job? = null


    init {
        loadPosts()
        startNewPostsChecker()
    }

    fun loadPosts() {
        _state.value = FeedModelState(loading = true)
        viewModelScope.launch {
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun startNewPostsChecker() {
        viewModelScope.launch {
            // Получаем Flow с количеством новых постов
            repository.getNewer(latestPostId).collect { count ->
                if (count > 0) {
                    _newPostsState.value = NewPostsState.Available(count)
                }
            }
        }
    }

    fun loadNewPosts() {
        viewModelScope.launch {
            _newPostsState.value = NewPostsState.Loading
            try {
                val newPostsCount = repository.loadNewPosts(latestPostId).size
                if (newPostsCount > 0) {
                    _newPostsState.value = NewPostsState.Loaded(newPostsCount)
                } else {
                    _newPostsState.value = NewPostsState.Hidden
                }
            } catch (e: Exception) {
                _newPostsState.value = NewPostsState.Error
            }
        }
    }

    fun showNewPosts() {
        // Обновляем основной список
        loadPosts()
        // Сбрасываем состояние новых постов
        _newPostsState.value = NewPostsState.Hidden
        // Перезапускаем проверку с новым latestPostId
        startNewPostsChecker()
    }

    fun retryNewPosts() {
        _newPostsState.value = NewPostsState.Hidden
        startNewPostsChecker()
    }

    override fun onCleared() {
        super.onCleared()
        newPostsJob?.cancel()
    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                repository.save(it)
            }
            _postCreated.value = Unit
        }
        edited.value = empty
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
        viewModelScope.launch {
            try {
                repository.likeById(id)
                loadPosts() // Обновляем список после изменения
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                loadPosts() // Обновляем список после удаления
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun refresh() {
        _state.value = FeedModelState(refreshing = true)
        viewModelScope.launch {
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
}