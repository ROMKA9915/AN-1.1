package ru.netology.nmedia.repository

import android.view.View
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.netology.nmedia.MainActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = ""
)

class PostViewModel: ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun edit(post:Post) {
        edited.value = post
    }

    fun changeContentAndSave(text :String) {
        edited.value?.let {
            if (it.content != text) {
                repository.save(it.copy(content = text))
            }
        }
        edited.value = empty
    }

    fun clearEdit() { edited.value = empty }
}