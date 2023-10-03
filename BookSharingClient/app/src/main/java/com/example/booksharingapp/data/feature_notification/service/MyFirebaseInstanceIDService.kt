/**
 * File: MyFirebaseInstanceIDService.kt
 * Author: Martin Baláž
 * Description: This file contains service class, that is responsible for handling the creation, rotation, and updating of registration tokens.
 */

package com.example.booksharingapp.data.feature_notification.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This is a service class that handles the creation, rotation, and updating of registration tokens.
 */
class MyFirebaseInstanceIDService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        // This service does not provide binding, so return null
        return null
    }

}