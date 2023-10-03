/**
 * File: MainActivity.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/MainActivity.kt
 * Description: This file contains MainActivity class, which is an Android activity that serves as the entry point for the application.
 */

package com.example.booksharingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import coil.annotation.ExperimentalCoilApi
import com.example.booksharingapp.data.UserPreferences
import com.example.booksharingapp.ui.auth.AuthActivity
import com.example.booksharingapp.ui.home.HomeActivity
import com.example.booksharingapp.ui.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalCoilApi
/**
 * This class serves as the entry point for the application.
 * Class is extending the ComponentActivity class to take advantage of its lifecycle-aware features, such as LiveData observation.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show logo when turning app on
        setContentView(R.layout.activity_main)

        // Create an instance of UserPreferences with a reference to the current activity.
        val userPreferences = UserPreferences(this)

        userPreferences.authToken.asLiveData().observe(this, Observer {
            // If we have locally stored Authentication Token, go to Authentication screen
            val activity = if(it == null) AuthActivity::class.java else HomeActivity::class.java
            startNewActivity(activity)
        })
    }

}