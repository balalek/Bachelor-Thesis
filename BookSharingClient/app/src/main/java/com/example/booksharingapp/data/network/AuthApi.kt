/**
 * File: AuthApi.kt
 * Author: Martin Baláž
 * Description: This file contains interface, that defines the endpoints for authentication related API calls.
 */

package com.example.booksharingapp.data.network

import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.data.responses.LoginUser
import com.example.booksharingapp.data.responses.RegisterUser
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * This interface defines the endpoints for authentication related API calls.
 */
interface AuthApi {

    /**
     * Sends a login request to the server.
     * @param user The user object containing login credentials and registration token of used mobile device
     * @return The response object containing the user's access token among other things
     */
    @POST("auth/login")
    suspend fun login(
        @Body user: LoginUser
    ): ApiResponse

    /**
     * Sends a register request to the server.
     * @param user The user object containing registration information
     * @return The response object containing success/failure response
     */
    @POST("auth/register")
    suspend fun register(
        @Body user: RegisterUser
    ): ApiResponse

}