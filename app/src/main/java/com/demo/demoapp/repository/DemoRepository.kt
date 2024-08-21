package com.demo.demoapp.repository

import android.util.Log
import com.demo.demoapp.data.DataOrException
import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import com.demo.demoapp.network.DemoApi
import javax.inject.Inject

class DemoRepository @Inject constructor(private val api: DemoApi) {
    suspend fun getPosts(): List<Post> = api.getPosts()
    suspend fun getComments():List<Comment> = api.getComments()
}