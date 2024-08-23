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
    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())

    private val _postsState = MutableStateFlow<DataOrException<List<Post>, Boolean, Exception>>(
        DataOrException(loading = true)
    )
    val postsState: StateFlow<DataOrException<List<Post>, Boolean, Exception>> =
        _postsState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 10

    private val _commentsState =
        MutableStateFlow<DataOrException<List<Comment>, Boolean, Exception>>(
            DataOrException(loading = true)
        )


    private val users = parseUserList()

    private val _selectedComments = MutableStateFlow<List<Comment>>(emptyList())
    val selectedComment = _selectedComments.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    init {
        getPostsAndComments()
    }

    private fun paginatePosts() {
        val fromIndex = (currentPage - 1) * pageSize
        val toIndex = kotlin.math.min(currentPage * pageSize, _allPosts.value.size)
        if (fromIndex < toIndex) {
            val paginatedPosts = _allPosts.value.subList(fromIndex, toIndex)
            _postsState.value = DataOrException(
                data = (_postsState.value.data ?: emptyList()) + paginatedPosts,
                loading = false,
                e = null
            )
            currentPage++
        }
    }

    fun getAllPostsSize(): Int {
        return _allPosts.value.size
    }

    fun searchForPostByTitle(title: String) {
        viewModelScope.launch {
            val filteredPosts = if (title.isEmpty()) {
                _allPosts.value
            } else {
                _allPosts.value.filter { it.title.contains(title, ignoreCase = true) }
            }
            _postsState.value = DataOrException(
                data = filteredPosts,
                loading = false,
                e = null
            )
        }
    }


    fun getPostsAndComments() {
        viewModelScope.launch {
            val postsDeferred = async { repository.getPosts() }
            val commentsDeferred = async { repository.getComments() }

            val postsResult = postsDeferred.await()
            val commentsResult = commentsDeferred.await()

            if (postsResult.data != null) {
                _allPosts.value = postsResult.data!!
                paginatePosts()
            }

            _commentsState.value = commentsResult
        }
    }

    fun loadMorePosts() {
        paginatePosts()
    }

    fun getCommentsForPost(postId: Int): List<Comment> {
        return _commentsState.value.data?.filter { it.postId == postId } ?: emptyList()
    }

    fun getUsernameById(userId: Int): String {
        return users.find { it.id == userId }?.username ?: "Unknown"
    }

    fun getCommentsCountsByPostId(postId: Int): Int {
        return getCommentsForPost(postId).size
    }

    fun getCommentWithUser(comment: Comment): Pair<Comment, User?> {
        val user = users.find { it.email == comment.email }
        return comment to user
    }

    fun setSelectedComments(comment: List<Comment>) {
        _selectedComments.value = comment
        _isSheetOpen.value = true
    }

    fun dismissSheet() {
        _isSheetOpen.value = false
    }
}

