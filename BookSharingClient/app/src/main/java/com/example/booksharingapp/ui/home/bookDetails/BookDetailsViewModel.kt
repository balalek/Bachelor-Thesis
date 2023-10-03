/**
 * File: BookDetailsViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for retrieving all book details, making requests to borrow book and notify me, or delete those requests in BookDetailsFragment.
 */

package com.example.booksharingapp.ui.home.bookDetails

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
 * This class is used for retrieving all book details, making requests to borrow book or notify me, or delete those requests in BookDetailsFragment.
 * @param repository The book repository to handle book details, requesting for borrow/notification and their deletion requests
 */
class BookDetailsViewModel(
        private val repository: BookRepository
): BaseViewModel(repository){

        // MutableLiveData to hold the book details response data
        private val _bookInfo: MutableLiveData<Resource<BookInfoResponse>> = MutableLiveData()
        val bookInfo: LiveData<Resource<BookInfoResponse>>
                get() = _bookInfo

        /**
         * This function handles the retrieval of book details to specified book.
         * @param bookId The ID of book, about which we want details from server
         * @return A [LiveData] object containing a [Resource] with the book details
         */
        fun getBookInfo(bookId: Int) = viewModelScope.launch {
                _bookInfo.value = Resource.Loading
                _bookInfo.value = repository.getBookInfo(bookId)
        }

        // MutableLiveData to hold the borrow book request response data
        private val _bookToBorrow: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
        val bookToBorrow: LiveData<Resource<ApiResponse>>
                get() = _bookToBorrow

        /**
         * This function handles the borrow book request operation.
         * @param bookId The ID of unborrowed book, that logged-in user wants to borrow
         * @return A [LiveData] object containing a [Resource] with the result of the operation
         */
        fun borrowBook(bookId: Int) = viewModelScope.launch {
                _bookToBorrow.value = Resource.Loading
                _bookToBorrow.value = repository.borrowBook(bookId)
        }

        // MutableLiveData to hold the notify me request response data
        private val _bookToNotify: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
        val bookToNotify: LiveData<Resource<ApiResponse>>
                get() = _bookToNotify

        /**
         * This function handles the notify me request operation.
         * @param bookId The ID of borrowed book, that logged-in user wants to borrow, once its available
         * @return A [LiveData] object containing a [Resource] with the result of the operation
         */
        fun notifyMe(bookId: Int) = viewModelScope.launch {
                _bookToNotify.value = Resource.Loading
                _bookToNotify.value = repository.notifyMe(bookId)
        }

        // MutableLiveData to hold delete request response data
        private val _bookDeleteRequest: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
        val bookDeleteRequest: LiveData<Resource<ApiResponse>>
                get() = _bookDeleteRequest

        /**
         * This function handles the delete request operation.
         * @param bookId The ID of book, on which logged-in user wants to make delete request
         * @return A [LiveData] object containing a [Resource] with the result of the operation
         */
        fun deleteRequest(bookId: Int) = viewModelScope.launch {
                _bookDeleteRequest.value = Resource.Loading
                _bookDeleteRequest.value = repository.deleteRequest(bookId)
        }
}