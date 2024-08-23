package com.demo.demoapp.screns

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.demo.demoapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoApp(
    modifier: Modifier = Modifier
){
    val bottomSheetState = rememberModalBottomSheetState()

    val viewModel: HomeScreenViewModel = hiltViewModel()
    val postsUiState = viewModel.postsUiState
    val commentsUiState = viewModel.commentsUiState


    Box(
        modifier = modifier
    ) {

        Column {
            TextField(
                value = viewModel.searchText,
                onValueChange = { viewModel.updateSearchText(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(R.string.search_posts))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            when (postsUiState) {

                is PostsUiState.Success -> {
                    PostsListDisplay(
                        posts = postsUiState.postList,
                        count = { postId ->
                            viewModel.getCommentsCountsByPostId(postId)
                        },
                        onClick = { postId ->
                            viewModel.onPostClicked(postId)
                        },
                        findAuthor = { userId ->
                            viewModel.getUserById(userId).name
                        },
                        loadPosts = {
                            viewModel.loadMorePosts()
                        },
                        maxIndex = viewModel.postsToDisplay.size - 1
                    )

                    if (viewModel.isSheetOpen) {
                        ModalBottomSheet(
                            sheetState = bottomSheetState,
                            onDismissRequest = { viewModel.onDismiss() }
                        ) {
                            when (commentsUiState) {
                                is CommentsUiState.Success ->
                                    CommentsBottomSheet(
                                        comments = viewModel.selectedComments,
                                        user = { email ->
                                            viewModel.getUserByEmail(email)
                                        }
                                    )

                                CommentsUiState.Error -> ErrorScreen(retryAction = { viewModel.getComments() })
                                CommentsUiState.Loading -> LoadingScreen()
                            }
                        }
                    }
                }

                PostsUiState.Error -> ErrorScreen(
                    retryAction = {
                        viewModel.searchText = ""
                        viewModel.getPosts()
                    }
                )
                PostsUiState.Loading -> LoadingScreen()
            }
        }
    }
}


@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun ErrorScreen(
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.unable_to_load_data), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(text = stringResource(R.string.retry))
        }
    }
}