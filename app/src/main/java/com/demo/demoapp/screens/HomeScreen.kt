package com.demo.demoapp.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.demoapp.R
import com.demo.demoapp.components.InputField
import com.demo.demoapp.components.PostsTopAppBar
import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsApp(viewModel: HomeViewModel) {
    val posts by viewModel.postsState.collectAsState()
    val context = LocalContext.current
    val selectedComments by viewModel.selectedComment.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()
    val isSheetOpen by viewModel.isSheetOpen.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchQuery = rememberSaveable {
        mutableStateOf("")
    }


    when {
        posts.loading == true && posts.data.isNullOrEmpty() -> {
            LoadingScreen()
        }

        posts.data != null -> {
            Scaffold(
                topBar = {
                    PostsTopAppBar(title = "Posts")
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Column {
                        InputField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            valueState = searchQuery,
                            labelId = "Search posts",
                            onAction = KeyboardActions {
                                viewModel.searchForPostByTitle(searchQuery.value.trim())
                                keyboardController?.hide()
                            }
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(items = posts.data!!) { index, post ->
                                val username = viewModel.getUsernameById(post.userId)
                                val commentCount = viewModel.getCommentsCountsByPostId(post.id)
                                val coroutineScope = rememberCoroutineScope()
                                PostRow(
                                    post = post,
                                    commentSize = commentCount,
                                    username = username,
                                    onClick = {
                                        viewModel.setSelectedComments(
                                            viewModel.getCommentsForPost(
                                                post.id
                                            )
                                        )
                                    })
                                if (index >= posts.data!!.size - 1 && index < viewModel.getAllPostsSize() - 1) {
                                    CircularProgressIndicator()
                                    coroutineScope.launch {
                                        delay(500)
                                        viewModel.loadMorePosts()
                                    }
                                }
                            }
                        }

                    }


                }
            }

            if (isSheetOpen) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = {
                        viewModel.dismissSheet()
                    }
                ) {
                    CommentsBottomSheet(comments = selectedComments, viewModel = viewModel)
                }
            }

        }

        else -> ErrorScreen(retryAction = {
            viewModel.getPostsAndComments()
            Toast.makeText(context, "Retrying...", Toast.LENGTH_SHORT).show()
        })
    }
}


@Composable
fun CommentsBottomSheet(comments: List<Comment>, viewModel: HomeViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = comments) { comment ->
            val (commentData, user) = viewModel.getCommentWithUser(comment)
            CommentItem(comment = commentData, user = user)
        }
    }
}


@Composable
fun CommentItem(comment: Comment, user: User?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(corner = CornerSize(5.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = user?.username ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment.email,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun PostRow(
    post: Post,
    onClick: () -> Unit,
    commentSize: Int,
    username: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(corner = CornerSize(5.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(100.dp)
                    .padding(start = 10.dp),
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = "Placeholder image",
                contentScale = ContentScale.Crop
            )


            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Posted by: $username",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$commentSize comments",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun ErrorScreen(retryAction: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Error loading posts...", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                modifier = Modifier.width(200.dp),
                onClick = { retryAction.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "Retry")
            }
        }
    }
}
