/**
 * File: BookRepository.kt
 * Author: Martin Baláž
 * Description: This file contains repository class, that it used for handling book-related API calls.
 */

package com.example.booksharingapp.data.repository

import com.example.booksharingapp.data.network.BookApi
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.data.responses.Book
import com.example.booksharingapp.data.responses.BookInfoResponse

/**
 * This is repository class for handling book-related API calls. It extends BaseRepository class.
 * @param api An BookApi instance for making API calls
 */
class BookRepository(
    private val api: BookApi
) : BaseRepository() {

    /**
     * This function makes an API call to get logged-in user delivery place.
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun getDeliveryPlace() = safeApiCall {
        api.getDeliveryPlace()
    }

    /**
     * This function makes an API call to get details about selected book.
     * @param bookId The ID of book, about which we want details from server
     * @return [Resource] object containing either [BookInfoResponse] or an error on failure
     */
    suspend fun getBookInfo(bookId: Int) = safeApiCall {
        api.getBookInfo(bookId)
    }

    /**
     * This function makes an API call to store information, that logged-in user wants selected book, once its available.
     * @param bookId The ID of borrowed book, that logged-in user wants to borrow, once its available
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun notifyMe(bookId: Int) = safeApiCall {
        api.notifyMe(bookId)
    }

    /**
     * This function makes an API call to store information, that logged-in user is requesting to borrow selected book.
     * @param bookId The ID of unborrowed book, that logged-in user wants to borrow
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun borrowBook(bookId: Int) = safeApiCall {
        api.borrowBook(bookId)
    }

    /**
     * This function makes an API call to delete logged-in user's request to selected book.
     * @param bookId The ID of book, on which logged-in user wants to make delete request
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun deleteRequest(bookId: Int) = safeApiCall {
        api.deleteRequest(bookId)
    }

    /**
     * This function makes an API call to update logged-in user's selected book.
     * @param bookId The ID of book, on which logged-in user wants to make update request
     * @param name Book's name
     * @param picture Book's cover photo
     * @param author Book's author
     * @param state Book's condition
     * @param info Book's information
     * @param price Book's price
     * @param accessibility Book's accessibility
     * @param maxBorrowedTime Book's maximum time, it can be borrowed for
     * @param genre Book's list of genres
     * @param borrowOptions Book's list of borrow options
     * @param handoverPlace Book's place to handover personally
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun updateBook(
        bookId: Int,
        name: String,
        picture: String,
        author: String,
        state: String,
        info: String,
        price: Int,
        accessibility: String,
        maxBorrowedTime: Int,
        genre: List<String>,
        borrowOptions: List<String>,
        handoverPlace: String
    ) = safeApiCall {
        val data = Book(name, picture, author, state, info, price, accessibility, maxBorrowedTime, genre, borrowOptions, handoverPlace)
        api.updateBook(bookId, data)
    }

    /**
     * This function makes an API call to insert new book.
     * @param name Book's name
     * @param picture Book's cover photo
     * @param author Book's author
     * @param state Book's condition
     * @param info Book's information
     * @param price Book's price
     * @param accessibility Book's accessibility
     * @param maxBorrowedTime Book's maximum time, it can be borrowed for
     * @param genre Book's list of genres
     * @param borrowOptions Book's list of borrow options
     * @param handoverPlace Book's place to handover personally
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun postBook(
        name: String,
        picture: String,
        author: String,
        state: String,
        info: String,
        price: Int,
        accessibility: String,
        maxBorrowedTime: Int,
        genre: List<String>,
        borrowOptions: List<String>,
        handoverPlace: String
    ) = safeApiCall {
        val data = Book(name, picture, author, state, info, price, accessibility, maxBorrowedTime, genre, borrowOptions, handoverPlace)
        api.postBook(data)
    }

}