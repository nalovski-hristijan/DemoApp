package com.demo.demoapp.model

data class Comment(
    val body: String,
    val email: String,
    val id: Int,
    val name: String,
    val postId: Int
)