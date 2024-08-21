package com.demo.demoapp.screns

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.demo.demoapp.R
import com.demo.demoapp.model.Post

@Composable
fun PostsListDisplay(
    posts: List<Post>,
    count: (Int) -> Int,
    onClick: (Int) -> Unit,
    findAuthor: (Int) -> String,
    modifier: Modifier = Modifier
){
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
    ) {
        items(posts){ post ->
            PostDisplay(
                count = count(post.id),
                post = post,
                author = findAuthor(post.userId),
                modifier = Modifier.padding(bottom = 12.dp),
                onClick = {onClick(post.id)}
            )
        }
    }
}

@Composable
fun PostDisplay(
    post: Post,
    author: String,
    count: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    Card(
        onClick = {onClick(post.id)},
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_broken_image),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Column {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.comment),
                            contentDescription = stringResource(R.string.comments),
                            modifier = Modifier
                                .size(12.dp)
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

