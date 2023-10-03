/**
 * File: HomeViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for retrieving books in HomeFragment. It also handles retrieving data in header in menu.
 */

package com.example.booksharingapp.ui.home.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.BookCardResponse
import com.example.booksharingapp.data.responses.Filter
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for retrieving books in HomeFragment and also for retrieving data in header in menu.
 * @param repository The combined repository to handle books cards and menu header requests
 */
class HomeViewModel (
    private val repository: CombinedRepository
): BaseViewModel(repository){

    // MutableLiveData to hold the menu header response data
    private val _menu: MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val menu: LiveData<Resource<ApiResponse>>
        get() = _menu

    /**
     * This function handles the retrieval of logged-in user's information in menu header.
     * @return A [LiveData] object containing a [Resource] with the menu header information
     */
    fun getMenuInfo() = viewModelScope.launch {
        _menu.value = Resource.Loading
        _menu.value = repository.getMenuInfo()
    }

    // MutableLiveData to hold the book cards response data
    private val _books: MutableLiveData<Resource<BookCardResponse>> = MutableLiveData()
    val books: LiveData<Resource<BookCardResponse>>
        get() = _books

    /**
     * This function handles the retrieval of book cards.
     * @return A [LiveData] object containing a [Resource] with the list of book cards
     */
    fun getBooks() = viewModelScope.launch {
        _books.value = Resource.Loading
        _books.value = repository.getBooks()
    }

    // MutableLiveData to hold the book cards by searched string response data
    private val _searchBooks: MutableLiveData<Resource<BookCardResponse>> = MutableLiveData()
    val searchBooks: LiveData<Resource<BookCardResponse>>
        get() = _searchBooks

    /**
     * This function handles the retrieval of book cards by searched string contained within name.
     * @param query The searched string, that must be contained in book name
     * @return A [LiveData] object containing a [Resource] with the list of book cards
     */
    fun searchBooks(query: String) = viewModelScope.launch {
        _searchBooks.value = Resource.Loading
        _searchBooks.value = repository.getSearchedBooks(query)
    }

    // MutableLiveData to hold the book cards by applied filter settings response data
    private val _filteredBooks: MutableLiveData<Resource<BookCardResponse>> = MutableLiveData()
    val filteredBooks: LiveData<Resource<BookCardResponse>>
        get() = _filteredBooks

    /**
     * This function handles the retrieval of book cards based on the specified filters.
     * @param filter The [Filter] object, that contains filter setting, that must be applied to book cards unless null
     * @return A [LiveData] object containing a [Resource] with the list of book cards
     */
    fun getFilteredBooks(filter: Filter?) = viewModelScope.launch {
        _filteredBooks.value = Resource.Loading
        _filteredBooks.value = repository.getFilteredBooks(filter)
    }
}