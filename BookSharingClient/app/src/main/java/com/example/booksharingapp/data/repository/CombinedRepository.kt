/**
 * File: CombinedRepository.kt
 * Author: Martin Baláž
 * Description: This file contains repository class, that it used for handling book and user related API calls.
 */

package com.example.booksharingapp.data.repository

import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.responses.*
import com.example.booksharingapp.data.responses.notifications.IsConfirmed
import com.example.booksharingapp.data.responses.notifications.IsReturned
import com.example.booksharingapp.data.responses.notifications.NotificationsResponse
import com.example.booksharingapp.data.responses.notifications.ReviewUser
import com.example.booksharingapp.data.responses.profile.ChangeContacts
import com.example.booksharingapp.data.responses.profile.ProfileResponse

/**
 * This is repository class for handling book and user related API calls. It extends BaseRepository class.
 * @param api An CombinedApi instance for making API calls
 */
class CombinedRepository(
    private val api: CombinedApi
) : BaseRepository() {

    /**
     * This function makes an API call to get logged-in user profile information.
     * @return [Resource] object containing either [ProfileResponse] or an error on failure
     */
    suspend fun getUser() = safeApiCall {
        api.getUser()
    }

    /**
     * This function makes an API call to get user's profile information by his ID.
     * @param userId The ID of user, whose profile information we want to get from server
     * @return [Resource] object containing either [ProfileResponse] or an error on failure
     */
    suspend fun getUserById(userId : Int) = safeApiCall {
        api.getUserById(userId)
    }

    /**
     * This function makes an API call to update logged-in user's password.
     * @param password The original password, that logged-in user wants to change
     * @param newPassword The new password, that logged-in user wants to apply
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun changePassword(
        password: String,
        newPassword: String
    ) = safeApiCall {
        val data = ChangePassword(password, newPassword)
        api.changePassword(data)
    }

    /**
     * This function makes an API call to update logged-in user's contacts.
     * @param profilePicture User's profile picture
     * @param userName User's username
     * @param email User's e-mail for contacting
     * @param handoverPlace User's place to handover books personally
     * @param postalCode User's postal code, where user can send book
     * @param phoneNumber User's phone number for contacting
     * @return [Resource] object containing either [ProfileResponse] or an error on failure
     */
    suspend fun changeContacts(
        profilePicture: String?,
        userName: String,
        email: String,
        handoverPlace: String?,
        postalCode: Int?,
        phoneNumber: Int?
    ) = safeApiCall {
        val data = ChangeContacts(userName, email, phoneNumber, profilePicture, handoverPlace, postalCode)
        api.changeContacts(data)
    }

    /**
     * This function makes an API call to answer on borrow book notification.
     * @param bookId The ID of book that user wants to borrow from logged-in user
     * @param isConfirmed Boolean that depends on if user accepted or declined borrow
     * @param recordId The ID of notification record to delete after answering
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun answerToBorrowBook(
        bookId: Int,
        isConfirmed: Boolean,
        recordId: Int
    ) = safeApiCall {
        val data = IsConfirmed(isConfirmed, recordId)
        api.answerToBorrowBook(bookId, data)
    }

    /**
     * This function makes an API call to get logged-in user's menu items.
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun getMenuInfo() = safeApiCall {
        api.getMenuInfo()
    }

    /**
     * This function makes an API call to get all books in system.
     * @return [Resource] object containing either [BookCardResponse] or an error on failure
     */
    suspend fun getBooks() = safeApiCall {
        api.getBooks()
    }

    /**
     * This function makes an API call to get all notifications, that belongs to logged-in user.
     * @return [Resource] object containing either [NotificationsResponse] or an error on failure
     */
    suspend fun getNotifications() = safeApiCall {
        api.getNotifications()
    }

    /**
     * This function makes an API call to get specific book/s by its name or substring.
     * @param query The name of book/s or its substring, which we want to get from server
     * @return [Resource] object containing either [BookCardResponse] or an error on failure
     */
    suspend fun getSearchedBooks(query : String) = safeApiCall {
        api.getSearchedBooks(query)
    }

    /**
     * This function makes an API call to get specific book/s by filter settings.
     * @param filter The filter object, that contains all filter settings, that book must possess
     * @return [Resource] object containing either [BookCardResponse] or an error on failure
     */
    suspend fun getFilteredBooks(filter: Filter?) = safeApiCall {
        val price = filter?.price
        val author = filter?.author
        val genre = filter?.genre?.toList()
        val borrowOptions = filter?.borrow_options?.toList()
        val availability = filter?.availability?.toList()
        api.getFilteredBooks(price, author, genre, borrowOptions, availability)
    }

    /**
     * This function makes an API call to return book soon.
     * @param bookId The ID of borrowed book, that logged-in user wants to return soon
     * @param isReturned Boolean that depends on if user accepted soon return, or stated that book was never borrowed
     * @return [Resource] object containing either [ProfileResponse] or an error on failure
     */
    suspend fun returnSoon(bookId: Int, isReturned: Boolean) = safeApiCall {
        val gotReturned = IsReturned(isReturned)
        api.returnSoon(bookId, gotReturned)
    }

    /**
     * This function makes an API call to return book late, or decline return.
     * @param bookId The ID of borrowed book, that logged-in user wants to return late or decline return
     * @param isReturned Boolean that depends on if user accepted or declined late return
     * @param recordId The ID of notification record to delete after book return
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun returnLate(
        bookId: Int,
        isReturned: Boolean,
        recordId: Int
    ) = safeApiCall {
        val gotReturned = IsReturned(isReturned, recordId)
        api.returnLate(bookId, gotReturned)
    }

    /**
     * This function makes an API call to evaluate user, with whom logged-in user had borrowed book.
     * @param userId The ID of user to evaluate
     * @param recordId The ID of notification record to delete after book return
     * @param content The content of review
     * @param score The number of stars (0-5) that user gave within review
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun reviewUser(
        userId: Int,
        recordId: Int,
        content: String?,
        score: Int
    ) = safeApiCall {
        val data = ReviewUser(recordId, score, content)
        api.reviewUser(userId, data)
    }

    /**
     * This function makes an API call to delete logged-in user's selected book.
     * @param bookId The ID of unborrowed book, that logged-in user wants to delete
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun deleteBook(bookId: Int) = safeApiCall {
        api.deleteBook(bookId)
    }

    /**
     * This function makes an API call to delete logged-in user's account.
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun deleteAccount() = safeApiCall {
        api.deleteAccount()
    }

}