package ru.netology.nmedia.repository

sealed class NewPostsState {
    object Hidden : NewPostsState()
    data class Available(val count: Int) : NewPostsState()
    object Loading : NewPostsState()
    data class Loaded(val count: Int) : NewPostsState()
    object Error : NewPostsState()
}