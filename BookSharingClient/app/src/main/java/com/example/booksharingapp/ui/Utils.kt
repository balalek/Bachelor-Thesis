/**
 * File: Utils.kt
 * Authors: Martin Baláž, Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/ui/Utils.kt
 * Description: This file contains functions, that are used across the application, like error handling, enabling buttons, starting new activity etc.
 */

package com.example.booksharingapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.ui.auth.LoginFragment
import com.example.booksharingapp.ui.auth.RegisterFragment
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.home.addOrEditBook.AddOrEditBookFragment
import com.example.booksharingapp.ui.home.bookDetails.BookDetailsFragment
import com.example.booksharingapp.ui.home.changePassword.ChangePasswordFragment
import com.example.booksharingapp.ui.home.deleteAccount.DeleteAccountFragment
import com.example.booksharingapp.ui.home.main.HomeFragment
import com.example.booksharingapp.ui.home.profile.ContactsFragment
import com.example.booksharingapp.ui.home.profile.MyBooksFragment
import com.google.android.material.snackbar.Snackbar

// Constants, that represents each type of notification user can get
const val BORROW_BOOK = 1
const val CONTACT_OWNER = 2
const val CONTACT_BORROWER = 3
const val OWNER_DECLINED_BORROW = 4
const val EVALUATE_OWNER = 5
const val EVALUATE_BORROWER = 6
const val TIME_LIMIT_EXPIRED = 7
const val BOOK_IS_NOW_AVAILABLE = 8

/**
 * This is an extension function to start a new activity.
 * @param activity The activity class to start
 */
fun<A : Activity> Activity.startNewActivity(activity: Class<A>){
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

/**
 * This is an extension function to set the visibility of a view, mainly used for progress bar.
 * @param isVisible True if the view should be visible, false otherwise
 */
fun View.visible(isVisible: Boolean){
    visibility = if(isVisible) View.VISIBLE else View.GONE
}

/**
 * This is an extension function to enable or disable a view, mainly used for disabled buttons.
 * @param enabled True if the view should be enabled, false otherwise
 */
fun View.enable(enabled: Boolean){
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.9f
}

/**
 * This is an extension function to display a snackbar with a message and optional action, used for retry snackbar.
 * @param message The message to display in the snackbar
 * @param action An optional action to perform when the snackbar action is clicked
 */
fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snackbar.setAction("Retry"){
            it()
        }
    }
    snackbar.show()
}

/**
 * This is a utility function to handle API errors and displays appropriate messages to the user.
 * @param failure The failure response received from the API call
 * @param retry A function to retry the API call, if it failed due to a network error
 */
fun Fragment.handleApiError(
    failure: Resource.Failure,
    retry: (() -> Unit)? = null
){
    when{
        failure.isNetworkError -> requireView().snackbar("Zkontrolujte připojení k internetu", retry)
        // 422 Unprocessable Content
        failure.errorCode == 422 -> {
            if (this is ContactsFragment) {
                showErrorSnackbar(requireView(),"Nesmíte odstranit PSČ, když si půjčujete knihu s takovou možností.")
            }
        // 409 Conflict
        } failure.errorCode == 409 -> {
            if (this is RegisterFragment || this is ContactsFragment) {
                showErrorSnackbar(requireView(),"Zadaný e-mail, uživatelské jméno, nebo telefonní číslo již existuje.")
            } else if (this is BookDetailsFragment) {
                showErrorSnackbar(requireView(),"Kniha již byla bohužel vypůjčena.")
            }
        // 404 Not Found
        } failure.errorCode == 404 -> {
            when (this) {
                is HomeFragment -> {
                    showErrorSnackbar(requireView(),"Žádné knihy nejsou v databázi.")
                }
                is AddOrEditBookFragment, is MyBooksFragment, is BookDetailsFragment -> {
                    showErrorSnackbar(requireView(),"Kniha nebyla nalezena.")
                }
                is DeleteAccountFragment -> {
                    showErrorSnackbar(requireView(),"Uživatel nebyl nalezen.")
                }
            }
        // 403 Forbidden
        } failure.errorCode == 403 -> {
            if(this is BookDetailsFragment) {
                showErrorSnackbar(requireView(),"Zadejte nejdříve na svém profilu PSČ, poté zkuste znovu.")
            } else if(this is MyBooksFragment) {
                showErrorSnackbar(requireView(),"Knize vypršel časový limit, prosím vyřiďte vrácení knihy v oznámeních.")
            }
        // 401 Unauthorized
        } failure.errorCode == 401 -> {
            if(this is LoginFragment){
                showErrorSnackbar(requireView(),"Zadali jste neprávný e-mail, nebo heslo.")
            } else {
                (this as BaseFragment<*,*,*>).logout()
            }
        // 400 Bad Request
        } failure.errorCode == 400 -> {
            when (this) {
                is ChangePasswordFragment -> {
                    showErrorSnackbar(requireView(),"Zadali jste nesprávné aktuální heslo.")
                }
                is HomeFragment -> {
                    showErrorSnackbar(requireView(),"Zadaná kniha není půjčována.")
                }
                is ContactsFragment, is AddOrEditBookFragment -> {
                    showErrorSnackbar(requireView(),"Nesmíte odstranit místo pro předání knihy, když půjčujete knihu s takovou možností.")
                }
                is MyBooksFragment -> {
                    showErrorSnackbar(requireView(),"Kniha již neexistuje.")
                }
                is DeleteAccountFragment -> {
                    showErrorSnackbar(requireView(),"Nesmíte nikomu půjčovat knihu a nesmíte mít ani žádnou vypůjčenou!")
                }
                is BookDetailsFragment -> {
                    showErrorSnackbar(requireView(),"Nelze požádat o oznámení.")
                }
            }
        // Other errors, that will show error text defined on server
        } else -> {
            val error = failure.errorBody?.string().toString()
            showErrorSnackbar(requireView(), error)
        }
    }
}

/**
 * This function shows an error snackbar with the specified message and an error icon.
 * @param view The view to attach the snackbar to
 * @param message The message to display in the snackbar
 */
fun showErrorSnackbar(view: View, message: String) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val snackbarView = snackbar.view
    val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackbarText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_error_outline_24, 0, 0, 0)
    snackbarText.compoundDrawablePadding = 12
    snackbarText.setTextColor(Color.WHITE)
    snackbarView.setBackgroundColor(Color.parseColor("#d82c2c"))
    snackbarView.setPadding(0, 0, 0, 0)

    snackbar.show()
}

/**
 * This function shows a success snackbar with the specified message and a checkmark icon.
 * @param view The view to attach the snackbar to
 * @param message The message to display in the snackbar
 */
fun showSuccessSnackbar(view: View, message: String) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    val snackbarView = snackbar.view
    val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackbarText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_circle_outline_white_24, 0, 0, 0)
    snackbarText.compoundDrawablePadding = 12
    snackbarText.setTextColor(Color.WHITE)
    snackbarView.setBackgroundColor(Color.parseColor("#307c34"))
    snackbarView.setPadding(0, 0, 0, 0)

    snackbar.show()
}

/**
 * This is a singleton object containing global variables used throughout the application.
 */
object GlobalVariables {
    var isOnMyProfile: Boolean = false
    var cameFromNotifications: Boolean = false
}