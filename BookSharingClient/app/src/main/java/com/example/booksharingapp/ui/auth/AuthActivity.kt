/**
 * File: AuthActivity.kt
 * Author: Martin Baláž
 * Description: This file contains main activity for authentication-related screens. It also setups navigation between child fragments and it check if user have
 *              notification permissions.
 */

package com.example.booksharingapp.ui.auth

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.booksharingapp.R
import android.provider.Settings

/**
 * This class is main activity for authentication-related screens.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Check if notification permission is granted
        checkNotificationPermission()

        // Setup navigation for navigating between fragments
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

    }

    /**
     * Function, that will ask user for notification permission, once its installed (needed for android 13+).
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.areNotificationsEnabled()) {
                // Open notification settings for this app
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Navigate up in the navigation hierarchy or call the superclass method if there's no higher level
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}