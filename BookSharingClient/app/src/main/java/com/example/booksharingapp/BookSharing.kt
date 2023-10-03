/**
 * File: BookSharing.kt
 * Author: Akash Kamati
 * Original code: https://github.com/akash251/Push-Notification-In-Android/blob/master/app/src/main/java/com/akash/mynotificationapp/BaseApplication.kt
 * Description: This file contains main application class for the BookSharing app.
 */

package com.example.booksharingapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.booksharingapp.data.util.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
/**
 * This is the main application class for the BookSharing app. It extends the Application class.
 */
class BookSharing : Application() {

    // This method is called when the application is created
    override fun onCreate() {
        super.onCreate()
        // Get the NotificationManager service and create the notification channel
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
    }

    // This function creates a notification channel for push notifications
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a new notification channel object
            val channel = NotificationChannel(
                Constants.PUSH_NOTIFICATION_CHANNEL_ID,
                Constants.PUSH_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
            }
            // Register the notification channel with the NotificationManager
            notificationManager.createNotificationChannel(channel)
        } else {
            return
        }

    }

}