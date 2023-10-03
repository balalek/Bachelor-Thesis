/**
 * File: FilterViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for future filter fragment operations.
 */

package com.example.booksharingapp.ui.home.filter

import com.example.booksharingapp.data.repository.BookRepository
import com.example.booksharingapp.ui.base.BaseViewModel

/**
 * This class doesn't have any operations at the moment.
 * @param repository The book repository
 */
class FilterViewModel(
    private val repository: BookRepository
): BaseViewModel(repository) {

}