package com.demo.demoapp.repository

import android.util.Log
import com.demo.demoapp.data.DataOrException
import com.demo.demoapp.model.Comment
import com.demo.demoapp.model.Post
import com.demo.demoapp.model.User
import com.demo.demoapp.network.DemoApi
import javax.inject.Inject

class DemoRepository @Inject constructor(private val api: DemoApi) {

    suspend fun getPosts(): DataOrException<List<Post>, Boolean, Exception> {
        val result = DataOrException<List<Post>, Boolean, Exception>()
        try {
            result.loading = true
            result.data = api.getPosts()
        } catch (e: Exception) {
            Log.d("Posts", "getPosts: $e")
            result.loading = false
        }

        return result
    }

    suspend fun getUsers(): DataOrException<List<User>, Boolean, Exception> {
        val result = DataOrException<List<User>, Boolean, Exception>()
        try {
            result.loading = true
            result.data = api.getUsers()
        } catch (e: Exception) {
            Log.d("Users", "getPosts: $e")
            result.loading = false
        }

        return result
    }

    suspend fun getComments(): DataOrException<List<Comment>, Boolean, Exception> {
        val result = DataOrException<List<Comment>, Boolean, Exception>()
        try {
            result.loading = true
            result.data = api.getComments()
        } catch (e: Exception) {
            Log.d("Posts", "getPosts: $e")
            result.loading = false
        }

        return result
    }
}