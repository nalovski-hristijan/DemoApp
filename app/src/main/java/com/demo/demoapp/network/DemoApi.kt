package com.demo.demoapp.network

import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import retrofit2.http.GET
import javax.inject.Singleton

@Singleton
interface DemoApi {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("comments")
    suspend fun getComments(): List<Comment>
}