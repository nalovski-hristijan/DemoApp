package com.demo.demoapp.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.demoapp.data.DataOrException
import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import com.demo.demoapp.repository.DemoRepository
import com.demo.demoapp.utils.parseUserList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: DemoRepository) : ViewModel() {
    private val _postsState = MutableStateFlow<DataOrException<List<Post>, Boolean, Exception>>(
        DataOrException(loading = true)
    )
    val postsState: StateFlow<DataOrException<List<Post>, Boolean, Exception>> =
        _postsState.asStateFlow()

    private val _commentsState =
        MutableStateFlow<DataOrException<List<Comment>, Boolean, Exception>>(
            DataOrException(loading = true)
        )
    val commentsState: StateFlow<DataOrException<List<Comment>, Boolean, Exception>> =
        _commentsState.asStateFlow()

    private val users = parseUserList()

    init {
        getPostsAndComments()
    }

    fun getPostsAndComments() {
        viewModelScope.launch {
            val postsDeferred = async { repository.getPosts() }
            val commentsDeferred = async { repository.getComments() }

            val postsResult = postsDeferred.await()
            val commentsResult = commentsDeferred.await()

            _postsState.value = postsResult
            _commentsState.value = commentsResult
        }
    }

    fun getCommentsForPost(postId: Int): List<Comment> {
        return _commentsState.value.data?.filter { it.postId == postId } ?: emptyList()
    }

    fun getUsernameById(userId: Int): String {
        return users.find { it.id == userId }?.username ?: "Unknown"
    }


    fun getCommentsCountsByPostId(postId: Int) : Int{
        return getCommentsForPost(postId).size
    }

    fun getCommentWithUser(comment: Comment): Pair<Comment, User?> {
        val user = users.find { it.email == comment.email }
        return comment to user
    }

}
