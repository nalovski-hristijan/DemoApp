package com.demo.demoapp.screns

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.demoapp.model.Address
import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Company
import com.demo.demoapp.model.Geo
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import com.demo.demoapp.repository.DemoRepository
import com.demo.demoapp.utils.parseUserList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed interface PostsUiState{
    data class Success(val postList: List<Post>): PostsUiState
    data object Error: PostsUiState
    data object Loading: PostsUiState
}

sealed interface CommentsUiState{
    data class Success(val commentList: List<Comment>): CommentsUiState
    data object Error: CommentsUiState
    data object Loading: CommentsUiState
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(private val repository: DemoRepository) : ViewModel(){

    var postsUiState: PostsUiState by mutableStateOf(PostsUiState.Loading)
        private set

    private var allPosts: List<Post> by mutableStateOf(emptyList())
    var postsToDisplay: List<Post> by mutableStateOf(emptyList())



    private val pageSize = 10
    private var currentPage = 1

    var commentsUiState: CommentsUiState by mutableStateOf(CommentsUiState.Loading)
        private set

    private var commentsByPost: Map<Int, List<Comment>> by mutableStateOf(emptyMap())
    var selectedComments: List<Comment> by mutableStateOf(emptyList())
    var isSheetOpen by mutableStateOf(false)

    private val users = parseUserList()

    private val placeholderUser = User(
        name = "Guest User",
        address = Address("", Geo("",""),"", "", ""),
        company = Company("", "",""),
        email = "",
        id = -1,
        phone = "",
        username = "guestUser",
        website = ""
    )

    var searchText by mutableStateOf("")


    init {
        getPosts()
        getComments()
    }

    private fun searchPosts(){
        try {
            postsToDisplay = allPosts.filter {
                it.title.contains(searchText, ignoreCase = true)
            }
            postsUiState = PostsUiState.Loading
            if (postsToDisplay.isEmpty()) {
                postsUiState = PostsUiState.Error
            } else {
                currentPage = 1
                paginate()
            }
        } catch (e: IOException){
            postsUiState = PostsUiState.Error
        }
    }

    fun updateSearchText(string: String){
        searchText = string
        searchPosts()
    }

    // function for dismissing botomsheet
    fun onDismiss() {
        isSheetOpen = false
    }
    //when post is clicked selected comments are that posts comments and bottomsheet should open
    fun onPostClicked(postId: Int) {
        selectedComments = commentsByPost[postId] ?: emptyList()
        isSheetOpen = true
    }

    // Creates a Map<postId, List<Comment>> to easily find comments for each post
    private fun getCommentsByPostIdMap() {
        if (commentsUiState is CommentsUiState.Success){
            val comments = (commentsUiState as CommentsUiState.Success).commentList
            commentsByPost = comments.groupBy {
                it.postId
            }
        }
    }
    //function to retrieve number of comments under a post
    fun getCommentsCountsByPostId(postId: Int) : Int{
        return commentsByPost[postId]?.size ?: 0
    }
    //function to get a user with specified userId used to retrieve post Authors
    fun getUserById(userId: Int): User {
        return users.find { user ->
            user.id == userId
        } ?: placeholderUser
    }
    //function to get a user with specified email used to retrieve comment Authors
    fun getUserByEmail(userEmail: String) : User {
        return users.find { user ->
            user.email == userEmail
        } ?: placeholderUser
    }

    private fun paginate(){
        val fromIndex = (currentPage - 1) * pageSize
        val toIndex = kotlin.math.min(currentPage * pageSize, postsToDisplay.size)
        if (fromIndex < toIndex) {
            val paginatedPosts = postsToDisplay.subList(fromIndex, toIndex)
            postsUiState = PostsUiState.Success(
                postList =
                ((postsUiState as? PostsUiState.Success)?.postList ?: emptyList()) + paginatedPosts
            )
            currentPage++
        }
    }
    fun loadMorePosts() {
        paginate()
    }

    fun getPosts(){
        viewModelScope.launch {
            try {
                allPosts = repository.getPosts()
                postsToDisplay = allPosts
                paginate()
            } catch (e: IOException) {
                postsUiState = PostsUiState.Error
            }
        }
    }

    fun getComments(){
        viewModelScope.launch {
            try {
                val commentsTest = repository.getComments()


                /* Written for testing purposes because all posts in datasource have 5 comments each
                to use change commentsTest to var
                commentsTest = commentsTest + listOf(
                    Comment(
                        body = "test",
                        email = "test@test.com",
                        id = 501,
                        name = "testComment",
                        postId = 1
                    )
                )
                */


                commentsUiState = CommentsUiState.Success(commentsTest)
                getCommentsByPostIdMap()
            } catch (e: IOException) {
                commentsUiState = CommentsUiState.Error
            }
        }
    }
}




