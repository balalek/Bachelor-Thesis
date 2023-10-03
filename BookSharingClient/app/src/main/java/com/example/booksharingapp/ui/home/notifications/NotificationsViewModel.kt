/**
 * File: NotificationsViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for retrieving all notifications to logged-in user,
 *              answering to borrow book, book late return or evaluating user in NotificationsFragment.
 */

package com.example.booksharingapp.ui.home.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.data.responses.notifications.NotificationsResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for retrieving all notifications to logged-in user, answering to borrow book, book late return or evaluating user in NotificationsFragment.
 * @param repository The combined repository to handle notification-related requests
 */
class NotificationsViewModel (
    private val repository: CombinedRepository
): BaseViewModel(repository){

    // MutableLiveData to hold the notifications response data
    private val _notifications: MutableLiveData<Resource<NotificationsResponse>> = MutableLiveData()
    val notifications: LiveData<Resource<NotificationsResponse>>
        get() = _notifications

    /**
     * This function handles the retrieval of logged-in user's notifications.
     * @return A [LiveData] object containing a [Resource] with the list of notifications
     */
    fun getNotifications() = viewModelScope.launch {
        _notifications.value = Resource.Loading
        _notifications.value = repository.getNotifications()
    }

    // MutableLiveData to hold the book borrow answer response data
    private val _answer: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val answer: LiveData<Resource<ApiResponse>>
        get() = _answer

    /**
     * This function handles the answer to book borrow request operation.
     * @param bookId The ID of book that user wants to borrow from logged-in user
     * @param isConfirmed Boolean that depends on if user accepted or declined borrow
     * @param recordId The ID of notification record to delete after answering
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun answerToBorrowBook(bookId: Int, isConfirmed: Boolean, recordId: Int) = viewModelScope.launch {
        _answer.value = Resource.Loading
        _answer.value = repository.answerToBorrowBook(bookId, isConfirmed, recordId)
    }

    // MutableLiveData to hold the review response data
    private val _review: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val review: LiveData<Resource<ApiResponse>>
        get() = _review

    /**
     * This function handles the evaluation of user operation.
     * @param userId The ID of user to evaluate
     * @param recordId The ID of notification record to delete after book return
     * @param content The content of review
     * @param score The number of stars (0-5) that user gave within review
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun reviewUser(userId: Int, recordId: Int, content: String?, score: Int) = viewModelScope.launch {
        _review.value = Resource.Loading
        _review.value = repository.reviewUser(userId, recordId, content, score)
    }

    // MutableLiveData to hold the return book late response data
    private val _returnLate: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val returnLate: LiveData<Resource<ApiResponse>>
        get() = _returnLate

    /**
     * This function handles the late book return operation.
     * @param bookId The ID of borrowed book, that logged-in user wants to return late or decline return
     * @param isReturned Boolean that depends on if user accepted or declined late return
     * @param recordId The ID of notification record to delete after book return
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun returnLate(bookId: Int, isReturned: Boolean, recordId: Int) = viewModelScope.launch {
        _returnLate.value = Resource.Loading
        _returnLate.value = repository.returnLate(bookId, isReturned, recordId)
    }

}