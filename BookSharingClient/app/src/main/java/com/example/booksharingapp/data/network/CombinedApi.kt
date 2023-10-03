/**
 * File: CombinedApi.kt
 * Author: Martin Baláž
 * Description: This file contains interface, that defines the endpoints for users and books related API calls.
 */

package com.example.booksharingapp.data.network

import com.example.booksharingapp.data.responses.*
import com.example.booksharingapp.data.responses.notifications.IsConfirmed
import com.example.booksharingapp.data.responses.notifications.IsReturned
import com.example.booksharingapp.data.responses.notifications.NotificationsResponse
import com.example.booksharingapp.data.responses.notifications.ReviewUser
import com.example.booksharingapp.data.responses.profile.ChangeContacts
import com.example.booksharingapp.data.responses.profile.ProfileResponse
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * This interface defines the endpoints for users and books related API calls.
 */
interface CombinedApi {

    /**
     * Sends a request to the server to get logged-in user profile information.
     * @return The response object containing the user's profile information
     */
    @GET("user")
    suspend fun getUser(): ProfileResponse

    /**
     * Sends a request to the server to get user's profile information by his ID.
     * @param userId The ID of user, whose profile information we want to get from server
     * @return The response object containing the user's profile information
     */
    @GET("user/{userId}")
    suspend fun getUserById(@Path("userId") userId: Int): ProfileResponse

    /**
     * Sends a request to the server to get logged-in user's menu items.
     * @return The response object containing the user's profile picture, username and e-mail
     */
    @GET("user/menuInfo")
    suspend fun getMenuInfo(): ApiResponse

    /**
     * Sends a request to the server to get all books in system.
     * @return The response object containing the books card information
     */
    @GET("books")
    suspend fun getBooks(): BookCardResponse

    /**
     * Sends a request to the server to get all notifications, that belongs to logged-in user.
     * @return The response object containing logged-in user notifications
     */
    @GET("user/notifications")
    suspend fun getNotifications(): NotificationsResponse

    /**
     * Sends a request to the server to get specific book/s by its name or substring.
     * @param bookName The name of book/s or its substring, which we want to get from server
     * @return The response object containing the books card information (with [bookName] in theirs name)
     */
    @GET("books/{bookName}")
    suspend fun getSearchedBooks(@Path("bookName") bookName: String): BookCardResponse

    /**
     * Sends a request to the server to get specific book/s by filter settings.
     * @param price The maximum price, that can book reach
     * @param author The name of author, that books must have
     * @param genre The list of genres, that books must contain
     * @param borrowOptions The list o borrow options, that books must contain
     * @param availability The list of availability, that books must contain
     * @return The response object containing the books card information (with filter settings applied to them)
     */
    @GET("books/filter")
    suspend fun getFilteredBooks(
        @Query("cena") price: Int?,
        @Query("autor") author: String?,
        @Query("zanr") genre: List<String>?,
        @Query("moznosti_pujceni") borrowOptions: List<String>?,
        @Query("dostupnost") availability: List<String>?
    ): BookCardResponse

    /**
     * Sends a logout request to the server.
     * @return The response object containing success/failure response
     */
    @POST("logout")
    suspend fun logout(): ApiResponse

    /**
     * Sends a request to the server to return book soon.
     * @param bookId The ID of borrowed book, that logged-in user wants to return soon
     * @param isReturned The object that contains Boolean value if book was returned or never borrowed
     * @return The response object containing success/failure response
     */
    @POST("book/{bookId}/returnSoon")
    suspend fun returnSoon(
        @Path("bookId") bookId: Int,
        @Body isReturned : IsReturned
    ): ProfileResponse

    /**
     * Sends a request to the server to return book late, or decline return.
     * @param bookId The ID of borrowed book, that logged-in user wants to return late or decline return
     * @param isReturned The object that contains Boolean value if book was returned or not
     * @return The response object containing success/failure response
     */
    @POST("book/{bookId}/returnLate")
    suspend fun returnLate(
        @Path("bookId") bookId: Int,
        @Body isReturned : IsReturned
    ): ApiResponse

    /**
     * Sends a request to the server to evaluate user, with whom logged-in user had borrowed book.
     * @param userId The ID of user to evaluate
     * @param review The review object that contains score and content of review
     * @return The response object containing success/failure response
     */
    @POST("user/{userId}/review")
    suspend fun reviewUser(
        @Path("userId") userId: Int,
        @Body review : ReviewUser
    ): ApiResponse

    /**
     * Sends a request to the server to answer on borrow book notification.
     * @param bookId The ID of book that user wants to borrow from logged-in user
     * @param isConfirmed The object that contains Boolean value if logged-in user accepted or declined borrow
     * @return The response object containing success/failure response
     */
    @POST("book/{bookId}/answer")
    suspend fun answerToBorrowBook(
        @Path("bookId") bookId: Int,
        @Body isConfirmed: IsConfirmed
    ): ApiResponse

    /**
     * Sends a request to the server to update logged-in user's contacts.
     * @param contacts The contacts object containing user's contacts, that logged-in user wants to update
     * @return The response object containing success/failure response
     */
    @PUT("user/contacts")
    suspend fun changeContacts(
        @Body contacts: ChangeContacts
    ): ProfileResponse

    /**
     * Sends a request to the server to update logged-in user's password.
     * @param user The object containing user's passwords, that logged-in user wants to change
     * @return The response object containing success/failure response
     */
    @PUT("user/changePassword")
    suspend fun changePassword(
        @Body user: ChangePassword
    ): ApiResponse

    /**
     * Sends a request to the server to delete logged-in user's selected book.
     * @param bookId The ID of unborrowed book, that logged-in user wants to delete
     * @return The response object containing success/failure response
     */
    @DELETE("book/{bookId}")
    suspend fun deleteBook(
        @Path("bookId") bookId: Int
    ): ApiResponse

    /**
     * Sends a request to the server to delete logged-in user's account.
     * @return The response object containing success/failure response
     */
    @DELETE("user/deleteAccount")
    suspend fun deleteAccount(): ApiResponse

}