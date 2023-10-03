/**
 * File: RemoteDataSource.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/data/network/RemoteDataSource.kt
 * Description: This file contains class, that is responsible for communicating with a remote API.
 */

package com.example.booksharingapp.data.network

import com.example.booksharingapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is responsible for communicating with a remote API.
 */
class RemoteDataSource {

    // BASE_URL is the base URL of the remote API.
    companion object {
        // The current base URL
        //const val BASE_URL = "http://147.229.216.179:8080"
        const val BASE_URL = "http://192.168.0.101:8080"
        //const val BASE_URL = "https://37d74519-ec3e-45c0-b35b-22032ff7e875.mock.pstmn.io" // Postman
    }

    /**
     * This method creates and returns a Retrofit API client.
     * @param api A class object that represents the API interface
     * @param authToken An optional authentication token to be passed in the HTTP request headers
     * @return An instance of the provided API interface, configured with the specified base URL and authentication token
     */
    fun<Api> buildApi(
        api: Class<Api>,
        authToken: String? = null
    ) : Api {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the Retrofit instance
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(chain.request().newBuilder().also {
                            // Add the Authorization header to the request if an auth token is provided
                            it.addHeader("Authorization", "Bearer $authToken")
                        }.build())
                    }.also { client ->
                    if (BuildConfig.DEBUG) {
                        // If in debug mode, add a logging interceptor to print the HTTP request and response details
                        val logging = HttpLoggingInterceptor()
                        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                        client.addInterceptor(logging)
                    }
                }.build()
            )
            .addConverterFactory(GsonConverterFactory.create()) // Add a Gson converter to convert between JSON and Kotlin objects
            .build()
            .create(api) // Create an instance of the provided API interface
    }

}