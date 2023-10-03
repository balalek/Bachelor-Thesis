/**
 * File: BookApi.kt
 * Author: Martin Baláž
 * Description: This file contains interface, that defines the endpoints for books related API calls.
 */

package com.example.booksharingapp.data.network

import com.example.booksharingapp.data.responses.Book
import com.example.booksharingapp.data.responses.BookInfoResponse
import com.example.booksharingapp.data.responses.ApiResponse
import retrofit2.http.*

/**
 * This interface defines the endpoints for books related API calls.
 */
interface BookApi {

    /**
     * Sends a request to the server to get logged-in user delivery place.
     * @return The response object containing the user's delivery place for book
     */
    @GET("book/deliveryPoint")
    suspend fun getDeliveryPlace(): ApiResponse


    /**
     * Sends a request to the server to get details about selected book.
     * @param bookId The ID of book, about which we want details from server
     * @return The response object containing selected book details
     */
    @GET("book/{bookId}")
    suspend fun getBookInfo(@Path("bookId") bookId: Int): BookInfoResponse

    /**
     * Sends a request to the server to insert new book.
     * @param book The book object containing all book-related information to store on server
     * @return The response object containing success/failure response
     */
    @POST("book")
    suspend fun postBook(
        @Body book: Book
    ): ApiResponse

    /**
     * Sends a request to the server to store information, that logged-in user wants selected book, once its available.
     * @param bookId The ID of borrowed book, that logged-in user wants to borrow, once its available
     * @return The response object containing success/failure response
     */
    @POST("book/{bookId}/notifyMe")
    suspend fun notifyMe(@Path("bookId") bookId: Int): ApiResponse

    /**
     * Sends a request to the server to store information, that logged-in user is requesting to borrow selected book.
     * @param bookId The ID of unborrowed book, that logged-in user wants to borrow
     * @return The response object containing success/failure response
     */
    @POST("book/{bookId}/borrow")
    suspend fun borrowBook(@Path("bookId") bookId: Int): ApiResponse

    /**
     * Sends a request to the server to update logged-in user's selected book.
     * @param bookId The ID of unborrowed book, that logged-in user wants to update
     * @param book The book object containing all book-related information to update on server
     * @return The response object containing success/failure response
     */
    @PUT("book/{bookId}")
    suspend fun updateBook(
        @Path("bookId") bookId: Int,
        @Body book: Book
    ): ApiResponse

    /**
     * Sends a request to the server to delete logged-in user's request to selected book.
     * @param bookId The ID of book, on which logged-in user wants to delete request
     * @return The response object containing success/failure response
     */
    @DELETE("book/{bookId}/deleteRequest")
    suspend fun deleteRequest(@Path("bookId") bookId: Int): ApiResponse

}