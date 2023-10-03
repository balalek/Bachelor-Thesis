/**
 * File: AddOrEditBookViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for adding or updating books in AddOrEditBookFragment.
 *              It is also used for retrieving logged-in user's handover place and book details in case of updating the book.
 */

package com.example.booksharingapp.ui.home.addOrEditBook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.BookRepository
import com.example.booksharingapp.data.responses.BookInfoResponse
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for adding of updating books in AddOrEditBookFragment and also for retrieving logged-in user's handover place and book details in case of updating the book.
 * @param repository The book repository to handle add/update book, handover place or book details requests
 */
class AddOrEditBookViewModel(
    private val repository: BookRepository
): BaseViewModel(repository){

    // MutableLiveData to hold the handover place response data
    private val _deliveryPlace: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val deliveryPlace: LiveData<Resource<ApiResponse>>
        get() = _deliveryPlace

    /**
    * This function handles the retrieval of logged-in user's handover place for adding a new book.
    * @return A [LiveData] object containing a [Resource] with the handover place information
    */
    fun getDeliveryPlace() = viewModelScope.launch {
        _deliveryPlace.value = Resource.Loading
        _deliveryPlace.value = repository.getDeliveryPlace()
    }

    // MutableLiveData to hold the add/update book response data
    private val _postOrPutBook: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val postOrPutBook: LiveData<Resource<ApiResponse>>
        get() = _postOrPutBook

    /**
     * This function handles the add book operation.
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
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun postBook(
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
    ) = viewModelScope.launch {
        _postOrPutBook.value = Resource.Loading
        _postOrPutBook.value = repository.postBook(name, picture, author, state, info, price, accessibility, maxBorrowedTime, genre, borrowOptions, handoverPlace)
    }

    /**
     * This function handles the update book operation.
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
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun putBook(
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
    ) = viewModelScope.launch {
        _postOrPutBook.value = Resource.Loading
        _postOrPutBook.value = repository.updateBook(bookId, name, picture, author, state, info, price, accessibility, maxBorrowedTime, genre, borrowOptions, handoverPlace)
    }

    // MutableLiveData to hold the original book details response data for updating book
    private val _bookInfo: MutableLiveData<Resource<BookInfoResponse>> = MutableLiveData()
    val bookInfo: LiveData<Resource<BookInfoResponse>>
        get() = _bookInfo

    /**
     * This function handles the retrieval of original book data for updating the the specified book.
     * @param bookId The ID of book, about which we want original data to update them
     * @return A [LiveData] object containing a [Resource] with the book details
     */
    fun getBookInfo(bookId: Int) = viewModelScope.launch {
        _bookInfo.value = Resource.Loading
        _bookInfo.value = repository.getBookInfo(bookId)
    }

}