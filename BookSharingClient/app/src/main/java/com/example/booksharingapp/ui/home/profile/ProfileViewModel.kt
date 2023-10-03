/**
 * File: ProfileViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for retrieving all profile information, updating contacts or delete/return book in ProfileFragment and its child fragments.
 *              It is also used for storing book lists across all fragments, that use this ViewModel.
 */

package com.example.booksharingapp.ui.home.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.data.responses.profile.BooksToReturn
import com.example.booksharingapp.data.responses.profile.BorrowedBooks
import com.example.booksharingapp.data.responses.profile.ProfileResponse
import com.example.booksharingapp.data.responses.profile.UnBorrowedBooks
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for retrieving all profile information, updating contacts or delete/return book in ProfileFragment and its child fragments.
 * It is also used for storing book lists across all fragments, that use this ViewModel.
 * @param repository The combined repository to handle profile information, update contacts and delete/return book requests
 */
class ProfileViewModel(
    private val repository: CombinedRepository
    ): BaseViewModel(repository){

    // MutableLiveData to hold the logged-in user profile response data
    private val _user: MutableLiveData<Resource<ProfileResponse>> = MutableLiveData()
    val user: LiveData<Resource<ProfileResponse>>
        get() = _user

    /**
     * This function handles the retrieval of logged-in user's profile information.
     * @return A [LiveData] object containing a [Resource] with the logged-in user's profile information
     */
    fun getUser() = viewModelScope.launch {
        _user.value = Resource.Loading
        _user.value = repository.getUser()
    }

    // MutableLiveData to hold the logged-in or other user profile response data
    private val _userById: MutableLiveData<Resource<ProfileResponse>> = MutableLiveData()
    val userById: LiveData<Resource<ProfileResponse>>
        get() = _userById

    /**
     * This function handles the retrieval of logged-in or other user's profile information.
     * @param userId The ID of user, that has been selected for retrieving his profile information
     * @return A [LiveData] object containing a [Resource] with the logged-in or other user's profile information
     */
    fun getUserById(userId : Int) = viewModelScope.launch {
        _userById.value = Resource.Loading
        _userById.value = repository.getUserById(userId)
    }

    // MutableLiveData to hold the change contacts response data
    private val _changeContactsResponse : MutableLiveData<Resource<ProfileResponse>> = MutableLiveData()
    val changeContactsResponse: LiveData<Resource<ProfileResponse>>
        get() = _changeContactsResponse

    /**
     * This function handles the change contacts operation.
     * @param profilePicture User's profile picture
     * @param userName User's username
     * @param email User's e-mail for contacting
     * @param handoverPlace User's place to handover books personally
     * @param postalCode User's postal code, where user can send book
     * @param phoneNumber User's phone number for contacting
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun changeContacts(
        profilePicture: String?,
        userName: String,
        email: String,
        handoverPlace: String?,
        postalCode: Int?,
        phoneNumber: Int?
    ) = viewModelScope.launch {
        _changeContactsResponse.value = Resource.Loading
        _changeContactsResponse.value = repository.changeContacts(profilePicture, userName, email, handoverPlace, postalCode, phoneNumber)
    }

    // MutableLiveData to hold book soon return response data
    private val _isReturned: MutableLiveData<Resource<ProfileResponse>> = MutableLiveData()
    val isReturned: LiveData<Resource<ProfileResponse>>
        get() = _isReturned

    /**
     * This function handles the book soon return operation.
     * @param bookId The ID of borrowed book, that logged-in user wants to return soon
     * @param gotReturned Boolean that depends on if user accepted soon return, or stated that book was never borrowed
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun returnSoon(bookId: Int, gotReturned: Boolean) = viewModelScope.launch {
        _isReturned.value = Resource.Loading
        _isReturned.value = repository.returnSoon(bookId, gotReturned)
    }

    // MutableLiveData to hold delete book response data
    private val _book: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val book: LiveData<Resource<ApiResponse>>
        get() = _book

    /**
     * This function handles the delete book operation.
     * @param bookId The ID of unborrowed book, that logged-in user wants to delete
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun deleteBook(bookId : Int) = viewModelScope.launch {
        _book.value = Resource.Loading
        _book.value = repository.deleteBook(bookId)
    }

    // MutableLiveData to hold unborrowed books list
    private val _books = MutableLiveData<List<UnBorrowedBooks>>()
    val books: LiveData<List<UnBorrowedBooks>> = _books

    /**
     * This function updates MutableLiveData that is holding unborrowed books list
     * @param newList The new list, that will replace current unborrowed books list
     */
    fun updateBookList(newList: List<UnBorrowedBooks>) {
        _books.value = newList
    }

    // MutableLiveData to hold borrowed books list
    private val _borrowedBooks = MutableLiveData<List<BorrowedBooks>>()
    val borrowedBooks: LiveData<List<BorrowedBooks>> = _borrowedBooks

    /**
     * This function updates MutableLiveData that is holding borrowed books list
     * @param newList The new list, that will replace current borrowed books list
     */
    fun updateBorrowedBookList(newList: List<BorrowedBooks>) {
        _borrowedBooks.value = newList
    }

    // MutableLiveData to hold books to return list
    private val _booksToReturn = MutableLiveData<List<BooksToReturn>>()
    val booksToReturn: LiveData<List<BooksToReturn>> = _booksToReturn

    /**
     * This function updates MutableLiveData that is holding books to return list
     * @param newList The new list, that will replace current books to return list
     */
    fun updateBooksToReturn(newList: List<BooksToReturn>) {
        _booksToReturn.value = newList
    }

}