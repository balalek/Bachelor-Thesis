/**
 * File: ViewModelFactory.kt
 * Author: Martin Baláž
 * Description: This file contains class, that is responsible for creating ViewModel instances by instantiating the corresponding repositories.
 */

package com.example.booksharingapp.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.booksharingapp.data.repository.AuthRepository
import com.example.booksharingapp.data.repository.BaseRepository
import com.example.booksharingapp.data.repository.BookRepository
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.ui.auth.AuthViewModel
import com.example.booksharingapp.ui.home.addOrEditBook.AddOrEditBookViewModel
import com.example.booksharingapp.ui.home.bookDetails.BookDetailsViewModel
import com.example.booksharingapp.ui.home.changePassword.ChangePasswordViewModel
import com.example.booksharingapp.ui.home.deleteAccount.DeleteAccountViewModel
import com.example.booksharingapp.ui.home.filter.FilterViewModel
import com.example.booksharingapp.ui.home.main.HomeViewModel
import com.example.booksharingapp.ui.home.notifications.NotificationsViewModel
import com.example.booksharingapp.ui.home.profile.ProfileViewModel

@Suppress("UNCHECKED_CAST")
/**
 * This class creates ViewModel instances by instantiating the corresponding repositories.
 * @param repository the base repository used to create the ViewModels
 */
class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            // Check if the given modelClass is assignable from AuthViewModel, and return an instance of AuthViewModel
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository as CombinedRepository) as T
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> NotificationsViewModel(repository as CombinedRepository) as T
            modelClass.isAssignableFrom(ChangePasswordViewModel::class.java) -> ChangePasswordViewModel(repository as CombinedRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository as CombinedRepository) as T
            modelClass.isAssignableFrom(FilterViewModel::class.java) -> FilterViewModel(repository as BookRepository) as T
            modelClass.isAssignableFrom(BookDetailsViewModel::class.java) -> BookDetailsViewModel(repository as BookRepository) as T
            modelClass.isAssignableFrom(AddOrEditBookViewModel::class.java) -> AddOrEditBookViewModel(repository as BookRepository) as T
            modelClass.isAssignableFrom(DeleteAccountViewModel::class.java) -> DeleteAccountViewModel(repository as CombinedRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }

}